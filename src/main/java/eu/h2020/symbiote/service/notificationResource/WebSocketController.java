/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.service.notificationResource;

import eu.h2020.symbiote.core.cci.accessNotificationMessages.SuccessfulAccessMessageInfo;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.communication.payloads.SecurityCredentials;
import org.omg.CORBA.ORB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.h2020.symbiote.resources.db.ResourcesRepository;
import eu.h2020.symbiote.cloud.model.rap.ResourceInfo;
import eu.h2020.symbiote.cloud.model.rap.access.ResourceAccessMessage;
import eu.h2020.symbiote.cloud.model.rap.access.ResourceAccessSubscribeMessage;
import eu.h2020.symbiote.model.cim.Observation;
import eu.h2020.symbiote.exceptions.EntityNotFoundException;
import eu.h2020.symbiote.interfaces.conditions.NBInterfaceWebSocketCondition;
import eu.h2020.symbiote.cloud.model.rap.access.ResourceAccessUnSubscribeMessage;
import eu.h2020.symbiote.interfaces.ResourceAccessNotificationService;
import eu.h2020.symbiote.resources.RapDefinitions;
import eu.h2020.symbiote.resources.db.PlatformInfo;
import eu.h2020.symbiote.resources.db.PluginRepository;
import eu.h2020.symbiote.resources.db.DbResourceInfo;
import eu.h2020.symbiote.security.commons.exceptions.custom.ValidationException;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.managers.AuthorizationManager;
import eu.h2020.symbiote.managers.AuthorizationResult;
import eu.h2020.symbiote.managers.ServiceResponseResult;
import eu.h2020.symbiote.service.notificationResource.WebSocketMessage.Action;
import static eu.h2020.symbiote.security.commons.SecurityConstants.SECURITY_RESPONSE_HEADER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 *
 * @author Luca Tomaselli
 */
@Conditional(NBInterfaceWebSocketCondition.class)
@Component
@CrossOrigin 
public class WebSocketController extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    ResourceAccessNotificationService notificationService;
    
    @Autowired
    ResourcesRepository resourcesRepo;
    
    @Autowired
    PluginRepository pluginRepo;
        
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    @Qualifier(RapDefinitions.PLUGIN_EXCHANGE_OUT)
    TopicExchange exchange;
    
    @Value("${symbiote.rap.cram.url}") 
    private String notificationUrl;
    
    @Autowired
    private AuthorizationManager authManager;

    private final HashMap<String, WebSocketSession> idSession = new HashMap<>();

    @Override
    public void handleTransportError(WebSocketSession session, Throwable throwable) throws Exception {
        log.error("error occured at sender " + session, throwable);
    }

    /**
     * This method handles the connection closed procedure.
     * @param session WebSocket session
     * @param status close status
     * @throws Exception exception in handling closing WebSocket
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Session " + session.getId() + " closed with status " + status.getCode());
        idSession.remove(session.getId());

        //update DB
        List<DbResourceInfo> resInfoList = resourcesRepo.findAll();
        if (resInfoList != null) {
            for (DbResourceInfo resInfo : resInfoList) {
                List<String> sessionsIdOfRes = resInfo.getSessionId();
                if (sessionsIdOfRes != null) {
                    sessionsIdOfRes.remove(session.getId());
                    resInfo.setSessionId(sessionsIdOfRes);
                    resourcesRepo.save(resInfo);
                }
            }
        }
    }//end

    /**
     * This method is called right after a client connects to the server
     *
     * @param session WebSocket session
     * @throws Exception exception in handling connection
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connected ... " + session.getId());
        idSession.put(session.getId(), session);
    }

    /**
     * This method is called whenever a message is received from the server
     *
     * @param session WebSocket session
     * @param jsonTextMessage received message
     * @throws Exception exception in handling message
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage jsonTextMessage) throws Exception  {
        Exception e = null;
        HttpStatusCode code = HttpStatusCode.INTERNAL_SERVER_ERROR;
        String message = "";
        try 
        {
            message = jsonTextMessage.getPayload();
            log.info("message received: " + message);

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);        
            WebSocketMessageSecurityRequest webSocketMessageSecurity = mapper.readValue(message, WebSocketMessageSecurityRequest.class);

            log.info("webSocketMessageSecurity message recognized");
            
            Map<String,String> securityRequest = webSocketMessageSecurity.getSecRequest();
            if(securityRequest == null) {
                log.error("Security Request cannot be empty");
                throw new Exception("Security Request cannot be empty");
            }
            
            WebSocketMessage webSocketMessage = webSocketMessageSecurity.getPayload();
            List<String> resourcesId = webSocketMessage.getIds();
            log.debug("Ids: " + resourcesId);
            log.info("Ids: " + resourcesId);

            //log.info("checkAccessPolicies called");
            String authenticationSize = securityRequest.get("x-auth-size").trim();

            /////////// IMPORTANT FOR EXTRA_INFO THE authenticationSize should be 0 ///////////////
            if(!authenticationSize.equals("0"))
              checkAccessPolicies(securityRequest,resourcesId);

            //log.info("checkAccessPolicies called ok");
            
            Action act = webSocketMessage.getAction();
            switch(act) {
                case SUBSCRIBE:
                    log.debug("Subscribing resources..");
                    log.info("Subscribing resources..");
                    Subscribe(session, resourcesId);
                    break;
                case UNSUBSCRIBE:
                    log.debug("Unsubscribing resources..");
                    log.info("Unsubscribing resources..");
                    UnsubscribeWithExtraInfo(session, resourcesId);
                    break;
                case EXTRA_INFO:
                    log.debug("Extra info received..");
                    log.info("Extra info received..");
                    SubscribeHandleExtraInfo(session, resourcesId);
                    break;
                case KEEP_ALIVE_MESSAGE:
                    log.info("keep alive message received, sending a keep alive message as response");
                    try {
                        session.sendMessage(new TextMessage("Keep alive message"));
                    }catch(Exception ex){
                        log.info("Exception in sending keep alive message");
                    }
                    break;
            }
        }catch (JsonParseException jsonEx){
            code = HttpStatusCode.BAD_REQUEST;
            e = jsonEx;
            log.error("Can not parse request", e);
        } catch (IOException ioEx) {
            code = HttpStatusCode.BAD_REQUEST;
            e = ioEx;
            log.error("Some IOException", e);
        } catch (EntityNotFoundException entityEx){
            code = HttpStatusCode.NOT_FOUND;
            e = entityEx;
            log.error("Can not find entity", e);
        } catch (ValidationException vex) {
        	log.error(vex.getMessage());        	
        } catch (Exception ex) {
            e = ex;
            log.error("Generic IO Exception: " + e.getMessage(), e);
        }
        
        if(e != null){
            session.sendMessage(new TextMessage(code.name()+ " "+
                    e.getMessage()));
            sendFailMessage(message,e);
        }
    }//end
//---------------------------------------------------------------------------------------------------------------
    private void SubscribeHandleExtraInfo(WebSocketSession session, List<String> resourcesId) throws Exception {

        System.out.println("HandleExtraInfo");

        String resourceId = resourcesId.get(0);
        String extraInfo  = resourcesId.get(1);

        System.out.println("resourceId = " + resourceId);
        System.out.println("extraInfo =  " + extraInfo);

        DbResourceInfo resInfo = getResourceInfo(resourceId);
        String pluginId        = resInfo.getPluginId();

        /*
         * if no plugin id specified,
         * we assume there's only one plugin attached.
         */

        if(pluginId == null) {
            List<PlatformInfo> lst = pluginRepo.findAll();

            if(lst == null || lst.isEmpty())
                throw new Exception("No plugin found");

            pluginId = lst.get(0).getPlatformId();
        }

        System.out.println("pluginId =  " + pluginId);

        //update DB
        List<String> sessionsIdOfRes = resInfo.getSessionId();

        if (sessionsIdOfRes == null) {
            System.out.println("sessionsIdOfRes == null");
            sessionsIdOfRes = new ArrayList<>();
        }
        sessionsIdOfRes.add(session.getId());
        System.out.println("session.getId() = " + session.getId());
        resInfo.setSessionId(sessionsIdOfRes);
        resourcesRepo.save(resInfo);

        //List<ResourceInfo> resList   = extraInfoList.get(plugin).stream().map(ri -> ri.toResourceInfo()).collect(Collectors.toList());
        System.out.println("1");
        List<ResourceInfo> resList  = new ArrayList<>();
        ResourceInfo resourceInfo   = new ResourceInfo();
        System.out.println("2");
        resourceInfo.setInternalId(extraInfo);
        System.out.println("3");
        resList.add(resourceInfo);
        System.out.println("4");
        ResourceAccessMessage msg    = new ResourceAccessSubscribeMessage(resList);
        System.out.println("5");
        String routingKey = pluginId + "." + ResourceAccessMessage.AccessType.SUBSCRIBE.toString().toLowerCase();
        System.out.println("6");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        System.out.println("7");
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        System.out.println("8");
        String json = mapper.writeValueAsString(msg);
        System.out.println("9");
        rabbitTemplate.convertSendAndReceive(exchange.getName(), routingKey, json);
        System.out.println("10");

    }//end
//------------------------------------------------------------------------------------------------------
    private void UnsubscribeWithExtraInfo(WebSocketSession session, List<String> resourcesId) throws Exception {

        System.out.println("UnsubscribeWithExtraInfo");

        String resourceId = resourcesId.get(0);
        String extraInfo  = resourcesId.get(1);

        System.out.println("resourceId = " + resourceId);
        System.out.println("extraInfo =  " + extraInfo);

        DbResourceInfo resInfo = getResourceInfo(resourceId);
        String pluginId        = resInfo.getPluginId();

        /*
         * if no plugin id specified,
         * we assume there's only one plugin attached.
         */

        if(pluginId == null) {
            List<PlatformInfo> lst = pluginRepo.findAll();

            if(lst == null || lst.isEmpty())
                throw new Exception("No plugin found");

            pluginId = lst.get(0).getPlatformId();
        }

        System.out.println("pluginId =  " + pluginId);

        //update DB
        List<String> sessionsIdOfRes = resInfo.getSessionId();

        if (sessionsIdOfRes == null) {
            System.out.println("sessionsIdOfRes == null");
            sessionsIdOfRes = new ArrayList<>();
        }
        /*
         * Remove the session id from
         * resources session list.
         */
        sessionsIdOfRes.remove(session.getId());
        System.out.println("session.getId() = " + session.getId());
        resInfo.setSessionId(sessionsIdOfRes);
        resourcesRepo.save(resInfo);

        //List<ResourceInfo> resList   = extraInfoList.get(plugin).stream().map(ri -> ri.toResourceInfo()).collect(Collectors.toList());
        System.out.println("1");
        List<ResourceInfo> resList  = new ArrayList<>();
        ResourceInfo resourceInfo   = new ResourceInfo();
        System.out.println("2");
        resourceInfo.setInternalId(extraInfo);
        System.out.println("3");
        resList.add(resourceInfo);
        System.out.println("4");
        ResourceAccessMessage msg    = new ResourceAccessUnSubscribeMessage(resList);
        System.out.println("5");
        String routingKey = pluginId + "." + ResourceAccessMessage.AccessType.UNSUBSCRIBE.toString().toLowerCase();
        System.out.println("6");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        System.out.println("7");
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        System.out.println("8");
        String json = mapper.writeValueAsString(msg);
        System.out.println("9");
        rabbitTemplate.convertSendAndReceive(exchange.getName(), routingKey, json);
        System.out.println("10");

    }//end
//---------------------------------------------------------------------------------------------------------------
    private void Subscribe(WebSocketSession session, List<String> resourcesId) throws Exception {
        HashMap<String, List<DbResourceInfo>> subscribeList = new HashMap<>();
        for (String resId : resourcesId) {
            // adding new resource info to subscribe map, with pluginId as key
            DbResourceInfo resInfo = getResourceInfo(resId);            
            String pluginId = resInfo.getPluginId();
            // if no plugin id specified, we assume there's only one plugin attached
            if(pluginId == null) {
                List<PlatformInfo> lst = pluginRepo.findAll();
                if(lst == null || lst.isEmpty())
                    throw new Exception("No plugin found");                
                pluginId = lst.get(0).getPlatformId();
            } 
            List<DbResourceInfo> rl;
            if(subscribeList.containsKey(pluginId)) {
                rl = subscribeList.get(pluginId);
            } else {
                rl = new ArrayList<>();
            }            
            rl.add(resInfo);
            subscribeList.put(pluginId, rl);
            //update DB
            List<String> sessionsIdOfRes = resInfo.getSessionId();
            if (sessionsIdOfRes == null) {
                sessionsIdOfRes = new ArrayList<>();
            }
            sessionsIdOfRes.add(session.getId());
            resInfo.setSessionId(sessionsIdOfRes);
            resourcesRepo.save(resInfo);
        }
        
        for(String plugin : subscribeList.keySet() ) {
            List<ResourceInfo> resList = subscribeList.get(plugin).stream().map(ri -> ri.toResourceInfo()).collect(Collectors.toList());
            ResourceAccessMessage msg = new ResourceAccessSubscribeMessage(resList);
            String routingKey = plugin + "." + ResourceAccessMessage.AccessType.SUBSCRIBE.toString().toLowerCase();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);        
            String json = mapper.writeValueAsString(msg);

            rabbitTemplate.convertSendAndReceive(exchange.getName(), routingKey, json);            
            sendSuccessfulAccessMessage(resourcesId, SuccessfulAccessMessageInfo.AccessType.SUBSCRIPTION_START.name());
        }
        
    }//end
    //-----------------------------------------------------------------------------------------------------------
    private void Unsubscribe(WebSocketSession session, List<String> resourcesId) throws Exception {
        HashMap<String, List<DbResourceInfo>> unsubscribeList = new HashMap<>();
        for (String resId : resourcesId) {
            // adding new resource info to subscribe map, with pluginId as key
            DbResourceInfo resInfo = getResourceInfo(resId);
            String pluginId = resInfo.getPluginId();
            // if no plugin id specified, we assume there's only one plugin attached
            if(pluginId == null) {
                List<PlatformInfo> lst = pluginRepo.findAll();
                if(lst == null || lst.isEmpty())
                    throw new Exception("No plugin found");                
                pluginId = lst.get(0).getPlatformId();
            } 
            List<DbResourceInfo> rl;
            if(unsubscribeList.containsKey(pluginId)) {
                rl = unsubscribeList.get(pluginId);
            } else {
                rl = new ArrayList<>();
            }            
            rl.add(resInfo);
            unsubscribeList.put(pluginId, rl);
            //update DB
            List<String> sessionsIdOfRes = resInfo.getSessionId();
            if (sessionsIdOfRes != null) {
                sessionsIdOfRes.remove(session.getId());
                resInfo.setSessionId(sessionsIdOfRes);
                resourcesRepo.save(resInfo);            
            }
        }
        for(String plugin : unsubscribeList.keySet() ) {
            List<ResourceInfo> resList = unsubscribeList.get(plugin).stream().map(ri -> ri.toResourceInfo()).collect(Collectors.toList());
            ResourceAccessMessage msg = new ResourceAccessUnSubscribeMessage(resList);
            String routingKey = plugin + "." + ResourceAccessMessage.AccessType.UNSUBSCRIBE.toString().toLowerCase();

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);        
            String json = mapper.writeValueAsString(msg);

            rabbitTemplate.convertSendAndReceive(exchange.getName(), routingKey, json);
            sendSuccessfulAccessMessage(resourcesId, SuccessfulAccessMessageInfo.AccessType.SUBSCRIPTION_END.name());
        }
    }//end


    /**
     * This method is used to send a push notification message to all the clients connected
     * and subscribed to the resource that emits the notification itself
     *
     * @param obs observation
     */
    public void SendMessage(Observation obs) {
        Map<String,String> secResponse = new HashMap<>();
        ServiceResponseResult serResponse = authManager.generateServiceResponse();

        log.info("SendMessage 1");
        if(serResponse.isCreatedSuccessfully()) {
            secResponse.put(SECURITY_RESPONSE_HEADER, serResponse.getServiceResponse());
        }        
        WebSocketMessageSecurityResponse messageSecurityResp = new WebSocketMessageSecurityResponse(secResponse, obs);
        log.info("SendMessage 2");
        
        String internalId = obs.getResourceId();//is the resource id not the internal id
        log.info("SendMessage 22 resourceId = " + internalId);//actually is the resource id
        DbResourceInfo resInfo =  getResourceInfo(internalId);//mxar getResourceByInternalId(internalId);
        log.info("SendMessage 23");
        List<String> sessionIdList = resInfo.getSessionId();
        log.info("SendMessage 24");
        HashSet<WebSocketSession> sessionList = new HashSet<>();
        log.info("SendMessage 3");
        if (sessionIdList != null && !sessionIdList.isEmpty()) {
            for (String sessionId : sessionIdList) {
                WebSocketSession session = idSession.get(sessionId);
                if(session != null)
                    sessionList.add(session);
            }
            log.info("SendMessage 4");
            String mess = "";
            try {
                ObjectMapper map = new ObjectMapper();
                map.configure(SerializationFeature.INDENT_OUTPUT, true);
                map.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                mess = map.writeValueAsString(messageSecurityResp);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            sendAll(sessionList, mess);
        }
    }

    private static void sendAll(Set<WebSocketSession> sessionList, String msg) {
        for (WebSocketSession session : sessionList) {
            try {
                session.sendMessage(new TextMessage(msg)); //.getBasicRemote().sendText(msg);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private DbResourceInfo getResourceInfo(String resId) {
        DbResourceInfo resInfo = null;
        Optional<DbResourceInfo> resInfoOptional = resourcesRepo.findById(resId);
        if(!resInfoOptional.isPresent())
            throw new EntityNotFoundException(resId);
        
        resInfo = resInfoOptional.get();
        return resInfo;
    }
    
    private DbResourceInfo getResourceByInternalId(String internalId) {
        DbResourceInfo resInfo = null;
        try {
            List<DbResourceInfo> resInfoList = resourcesRepo.findByInternalId(internalId);

            if (resInfoList != null && !resInfoList.isEmpty()) {
                for(DbResourceInfo ri: resInfoList){
                    resInfo = ri;
                    List<String> sessionsId = ri.getSessionId();
                    if(sessionsId != null && !sessionsId.isEmpty())
                        break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return resInfo;
    }

    /**
     * This method is used to send a successful access message to CRAM
     *
     * @param symbioteIdList list of symbiote IDs
     * @param accessType type of access from {@link eu.h2020.symbiote.core.cci.accessNotificationMessages.SuccessfulAccessMessageInfo.AccessType} name
     */
    public void sendSuccessfulAccessMessage(List<String> symbioteIdList, String accessType){
        List<Date> dateList = new ArrayList<Date>();
        dateList.add(new Date());
        
        notificationService.addSuccessfulAttemptsList(symbioteIdList, dateList, accessType);
        notificationService.sendAccessData();
    }
    
    private void sendFailMessage(String path, Exception e) {
        String appId = "";String issuer = ""; String validationStatus = "";
        String symbioteId = "";
        
        String code = Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR.value());
        String message = e.getMessage();
        if(message == null)
            message = e.toString();
        
        if(e.getClass().equals(EntityNotFoundException.class)){
            code = Integer.toString(HttpStatus.NOT_FOUND.value());
            symbioteId = ((EntityNotFoundException) e).getSymbioteId();
        }
            
        List<Date> dateList = new ArrayList<Date>();
        dateList.add(new Date());
        notificationService.addFailedAttempts(symbioteId, dateList, 
            code, message, appId, issuer, validationStatus, path);
        notificationService.sendAccessData();
    }

    /**
     * This method is used to check access policies towards Authentication Manager
     *
     * @param secHdrs map of security headers
     * @param resourceIdList list of resource IDs
     * @throws Exception security exception
     */
    public void checkAccessPolicies(Map<String, String> secHdrs, List<String> resourceIdList) throws Exception {
        
        log.debug("secHeaders11: " + secHdrs);
        log.info("secHeaders11: "  + secHdrs);

        Map<String, String> newSecHdrs = new HashMap<String, String>();

        newSecHdrs.put("x-auth-timestamp",secHdrs.get("x-auth-timestamp"));
        newSecHdrs.put("x-auth-size",secHdrs.get("x-auth-size"));

        String token = secHdrs.get("x-auth-1");
        String authenticationChallenge = secHdrs.get("authenticationChallenge");
        String clientCertificate       = secHdrs.get("clientCertificate");
        String clientCertificateSigningAAMCertificate = secHdrs.get("clientCertificateSigningAAMCertificate");
        String foreignTokenIssuingAAMCertificate      = secHdrs.get("foreignTokenIssuingAAMCertificate");


        String auth = "{\"token\":\"" + token + "\",\"authenticationChallenge\":\"" + authenticationChallenge + "\",\"clientCertificate\":\"" + clientCertificate+ "\",\"clientCertificateSigningAAMCertificate\":\"" + clientCertificateSigningAAMCertificate + "\",\"foreignTokenIssuingAAMCertificate\":\""+foreignTokenIssuingAAMCertificate +"\"}";

        //String auth = "{\"token\":\"" + token + "\",\"authenticationChallenge\":\"" + authenticationChallenge + "\",\"clientCertificate\":\"\",\"clientCertificateSigningAAMCertificate\":\"\",\"foreignTokenIssuingAAMCertificate\":\"\"}";
       // String auth = "{\"token\":\"" + token + "\",\"authenticationChallenge\":\"\",\"clientCertificate\":\"\",\"clientCertificateSigningAAMCertificate\":\"\",\"foreignTokenIssuingAAMCertificate\":\"\"}";

        newSecHdrs.put("x-auth-1",auth);

        log.debug("newSecHdrs.get(x-auth-timestamp)" + newSecHdrs.get("x-auth-timestamp"));
        log.debug("newSecHdrs.get(x-auth-size "      + newSecHdrs.get("x-auth-size"));
        log.debug("newSecHdrs.get( x-auth-1 "        + newSecHdrs.get("x-auth-1"));


        log.debug("newSecHdrs.toString() "    + newSecHdrs.toString());
        log.debug("newSecHdrs " + newSecHdrs);

        System.out.println("--------------------- using keySet-------------------------------");

        for (String key : newSecHdrs.keySet()) {
            System.out.println(key + "=" + newSecHdrs.get(key));
        }
        System.out.println();
        /////////////////////////////////////////////////////////////////////////

        log.info("calling  securityReq = new SecurityRequest(newSecHdrs)");
        SecurityRequest securityReq = new SecurityRequest(newSecHdrs);

        log.info("securityReq.getTimestamp()  = "               + securityReq.getTimestamp());
        log.info("securityReq.getSecurityCredentials() = "      + securityReq.getSecurityCredentials().toArray()[0].toString());
        log.info("securityReq.getProprietarySecurityPayload() " + securityReq.getProprietarySecurityPayload());

        log.info("calling  securityReq = new SecurityRequest(newSecHdrs) ok");

        for(String resourceId: resourceIdList){        
            AuthorizationResult result = authManager.checkResourceUrlRequest(resourceId, securityReq);
            log.info(result.getMessage());
            if(!result.isValidated()) {
                log.error("Resource " + resourceId + "access has been denied with message: " + result.getMessage());
                throw new ValidationException("The access policies were not satisfied for resource " + resourceId + ". MSG: " + result.getMessage());
            }
        }
    }//end

}//end of class
