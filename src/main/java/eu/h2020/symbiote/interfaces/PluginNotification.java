/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.model.cim.Observation;
import eu.h2020.symbiote.resources.db.ResourcesRepository;
import eu.h2020.symbiote.service.notificationResource.WebSocketController;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
//import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

import eu.h2020.symbiote.resources.db.DbResourceInfo;
import static eu.h2020.symbiote.resources.RapDefinitions.JSON_OBJECT_TYPE_FIELD_NAME;

/**
 *
* @author Luca Tomaselli
*/
public class PluginNotification {
    private static final Logger log = LoggerFactory.getLogger(PluginNotification.class);

    @Autowired
    private ResourceAccessNotificationService notificationService;
    
    @Autowired
    private WebSocketController webSocketController;
    
    @Value("${symbiote.rap.cram.url}") 
    private String notificationUrl;
    
    @Autowired
    private ResourcesRepository resourcesRepo;


    /**
     *
     * This method is receiving push notifications from plugin
     *
     * @param messageObject the message itself
     */
    public void receiveNotification(Object messageObject) {
        try {
            log.info("checkPushNotificationMessageFormat start");
            String message = checkPushNotificationMessageFormat(messageObject);//ok
            log.debug("Plugin Notification message received.\n" + message);

            // THIS WOULD CUT OUT SUPPORT FOR PIMs in notification mechanism
            log.debug("changeInternalIdToSymbIoTeIdInObservation");
            Observation observation = changeInternalIdToSymbIoTeIdInObservation(messageObject);

            log.debug("After changeInternalIdToSymbIoTeIdInObservation");
            sendSuccessfulPushMessage(observation.getResourceId());//needs internal id as parameter
            log.debug("After sendSuccessfulPushMessage");
            webSocketController.SendMessage(observation);
            log.debug("After webSocketController.SendMessage");
            
        } catch (Exception e) {
            log.info("Error while processing notification received from plugin \n" + e.getMessage());
        }
    }//end
//--------------------------------------------------------------------------------------------------------------------
    private DbResourceInfo getResourceInfo(String internalId) {
        DbResourceInfo resInfo = null;

        log.debug("internalId = ");//mxar
        log.debug(internalId);//mxar


        List<DbResourceInfo> resInfoList = resourcesRepo.findByInternalId(internalId);
        log.debug("resInfoList = " + resInfoList);//mxar

        if(resInfoList!= null){//mxar
            log.debug("resInfoList.size() = "+ resInfoList.size());
        }

        for (DbResourceInfo rInfo : resInfoList) {
            List<String> sessionList = rInfo.getSessionId();
            if(!sessionList.isEmpty()) {
                resInfo=rInfo;
                break;
            }
        }

        log.debug("resInfo = " + resInfo);//mxar
        return resInfo;
    }//end
//--------------------------------------------------------------------------------------------------------------------
    private String checkPushNotificationMessageFormat(Object obj) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        if (obj == null) {
            log.error("Empty notification from plugin");
            log.debug("Empty notification from plugin");
            throw new Exception("Empty notification from plugin");
        }
        log.debug("1");
        String rawObj;
        if (obj instanceof byte[]) {
            rawObj = new String((byte[]) obj, "UTF-8");
        } else if (obj instanceof String){
            rawObj = (String) obj;
        } else {
            throw new Exception("Can not parse response from RAP plugin. Expected byte[] or String but got " +
                    obj.getClass().getName());
        }
        log.debug("2");
        log.debug("rawObj = ");
        log.debug(rawObj);

        return  rawObj;
      /*  try {
            JsonNode jsonObj = mapper.readTree(rawObj);
            if (!jsonObj.has(JSON_OBJECT_TYPE_FIELD_NAME)) {
                log.error("Field " + JSON_OBJECT_TYPE_FIELD_NAME + " is mandatory");
            }
            return rawObj;
        } catch (Exception e) {
            throw new ODataApplicationException("Can not parse response from RAP to JSON.\n Cause: " + e.getMessage(),
                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
                    Locale.ROOT,
                    e);
        }*/
    }//end
//--------------------------------------------------------------------------------------------------------------------
    /*
     * Symbiote id is the resource id
     */
    private Observation changeInternalIdToSymbIoTeIdInObservation(Object obj) throws Exception {
        Observation internalObservation = null;
        String rawObj =""; //mxar
        ObjectMapper mapper = new ObjectMapper();//mxar

        if(obj instanceof Observation) {
            log.debug("obj instanceof Observation");
            internalObservation = (Observation) obj;
        } else if(obj instanceof Map) {
            log.debug("obj instanceof Map 1");
            mapper = new ObjectMapper();
            log.debug("obj instanceof Map 2");
            String jsonBody = mapper.writeValueAsString(obj);
            log.debug("obj instanceof Map 3");
            internalObservation = mapper.readValue(jsonBody, Observation.class);
            log.debug("obj instanceof Map 4");
        } else if (obj instanceof byte[]) {//mxar
            log.debug("obj instanceof byte[]");
            rawObj = new String((byte[]) obj, "UTF-8");
            log.debug("obj instanceof byte " + rawObj); //<------ this is called
        } else if (obj instanceof String){//mxar
            rawObj = (String) obj;
            log.debug("obj instanceof String1 " + rawObj);
        }
        else{
            log.debug("obj instanceof 111?????????");
        }

        ///////////////////////// mxar addition/////////////////////////////////////

        try {
            log.debug("observation  parsing... ");
            internalObservation = mapper.readValue(rawObj,Observation.class);
            log.debug("observation  parsing ends ... ");

            log.debug("internalObservation.getResultTime() = "   + internalObservation.getResultTime());
            log.debug("internalObservation.getSamplingTime() = " + internalObservation.getSamplingTime());
            log.debug("internalObservation.getResourceId() = "   + internalObservation.getResourceId());//<-- here shows internal id
            log.debug("internalObservation.getLocation() = "     + internalObservation.getLocation());

           /* log.debug("json parsing... ");
            JsonNode jsonObj = mapper.readTree(rawObj);
            log.debug("json parsing ends");

            log.debug("reading resource id ..");
            JsonNode resJsonNode = jsonObj.get("resourceId");
            String resId   = (String)resJsonNode.asText();
            log.debug("resId = " + resId);

            log.debug("reading resultTime ..");
            JsonNode resultTimeJsonNode = jsonObj.get("resultTime");
            String resultTime   = (String)resultTimeJsonNode.asText();
            log.debug("resultTime = " + resultTime);

            log.debug("reading samplingTime ..");
            JsonNode samplingTimeJsonNode = jsonObj.get("samplingTime");
            String samplingTime   = (String)samplingTimeJsonNode.asText();
            log.debug("samplingTime = " + samplingTime);*/

           // log.debug("reading location ..");
          //  JsonNode locatioJsonNode = jsonObj.get("location");
           // String locationAsString  = (String)locatioJsonNode.asText();
           // Location location        = mapper.readValue(locationAsString,Location.class);

            //log.debug("location = " + location.);

        }catch (Exception ex){
            log.debug("Exception json parsing");
        }



        log.debug("DbResourceInfo resInfo = getResourceInfo(internalObservation.getResourceId()) start");
        DbResourceInfo resInfo = getResourceInfo(internalObservation.getResourceId());//<--is the internal id
        log.debug("DbResourceInfo resInfo = getResourceInfo(internalObservation.getResourceId()) ends, resInfo.getSymbioteId() =  " + resInfo.getSymbioteId());
        //resInfo.getSymbioteId() is the resource id
        /*
         * Important Note !!!!!!!!
         * We have to clarify if we return the resource id
         * or internal id in the Observation object.
         *
         * I have replaced the resource id by internalid
         */
        return new Observation(resInfo.getSymbioteId()/*internalObservation.getResourceId()*/, internalObservation.getLocation(),
                internalObservation.getResultTime(), internalObservation.getSamplingTime(),
                internalObservation.getObsValues());
    }//end
//---------------------------------------------------------------------------------------------------------------
    /**
     * This method sent a successful push message to CRAM
     * @param symbioteId the id of the resource
     */
    public void sendSuccessfulPushMessage(String symbioteId){
        List<Date> dateList = new ArrayList<>();
        dateList.add(new Date());
        
        notificationService.addSuccessfulPushes(symbioteId, dateList);
        notificationService.sendAccessData();
    }//end

}//end of class
