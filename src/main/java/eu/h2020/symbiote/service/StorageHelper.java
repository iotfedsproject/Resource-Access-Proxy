/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.service;

import static eu.h2020.symbiote.resources.RapDefinitions.JSON_OBJECT_TYPE_FIELD_NAME;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.expression.Binary;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.h2020.symbiote.cloud.model.rap.ResourceInfo;
import eu.h2020.symbiote.cloud.model.rap.access.ResourceAccessGetMessage;
import eu.h2020.symbiote.cloud.model.rap.access.ResourceAccessHistoryMessage;
import eu.h2020.symbiote.cloud.model.rap.access.ResourceAccessMessage;
import eu.h2020.symbiote.cloud.model.rap.access.ResourceAccessSetMessage;
import eu.h2020.symbiote.cloud.model.rap.query.Comparison;
import eu.h2020.symbiote.cloud.model.rap.query.Filter;
import eu.h2020.symbiote.cloud.model.rap.query.Operator;
import eu.h2020.symbiote.cloud.model.rap.query.Query;
import eu.h2020.symbiote.core.cci.accessNotificationMessages.SuccessfulAccessMessageInfo;
import eu.h2020.symbiote.interfaces.ResourceAccessNotificationService;
import eu.h2020.symbiote.managers.AuthorizationManager;
import eu.h2020.symbiote.managers.AuthorizationResult;
import eu.h2020.symbiote.messages.plugin.RapPluginErrorResponse;
import eu.h2020.symbiote.messages.plugin.RapPluginOkResponse;
import eu.h2020.symbiote.messages.plugin.RapPluginResponse;
import eu.h2020.symbiote.model.cim.Actuator;
import eu.h2020.symbiote.model.cim.Capability;
import eu.h2020.symbiote.model.cim.Datatype;
import eu.h2020.symbiote.model.cim.Observation;
import eu.h2020.symbiote.model.cim.Parameter;
import eu.h2020.symbiote.model.cim.Service;
import eu.h2020.symbiote.resources.db.DbResourceInfo;
import eu.h2020.symbiote.resources.db.PlatformInfo;
import eu.h2020.symbiote.resources.db.PluginRepository;
import eu.h2020.symbiote.resources.db.ResourcesRepository;
import eu.h2020.symbiote.security.commons.exceptions.custom.ValidationException;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.validation.ValidationHelper;

/**
 *
 * @author Luca Tomaselli
 */
public class StorageHelper {
    private static final Logger log = LoggerFactory.getLogger(StorageHelper.class);
    
    private final int TOP_LIMIT = 100;
    
    private final ResourcesRepository resourcesRepo;
    private final PluginRepository pluginRepo;
    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange exchange;
    private final AuthorizationManager authManager;
    
    private boolean validateServiceRequestPayload = false;
    private boolean validateActuatorRequestPayload = false;
    private boolean validateServiceResponsePayload = false;

    private static final Pattern PATTERN = Pattern.compile(
            "\\p{Digit}{1,4}-\\p{Digit}{1,2}-\\p{Digit}{1,2}"
            + "T\\p{Digit}{1,2}:\\p{Digit}{1,2}(?::\\p{Digit}{1,2})?"
            + "(Z|([-+]\\p{Digit}{1,2}:\\p{Digit}{2}))?");

	private ResourceAccessNotificationService notificationService;

    /**
     * Class constructor
     * @param resourcesRepository   repository of resources
     * @param pluginRepository      repository of plugins
     * @param authMan               authorization manager instance
     * @param rabbit                rabbit template
     * @param rabbitReplyTimeout    rabbit reply timeout constant
     * @param topicExchange         rabbit exchange
     * @param notificationService   service to handle the notification to Monitoring
     */
    public StorageHelper(ResourcesRepository resourcesRepository, PluginRepository pluginRepository,
            AuthorizationManager authMan, RabbitTemplate rabbit, int rabbitReplyTimeout, 
            TopicExchange topicExchange, ResourceAccessNotificationService notificationService) {
        //initSampleData();
        resourcesRepo = resourcesRepository;
        pluginRepo = pluginRepository;
        rabbitTemplate = rabbit;
		this.notificationService = notificationService;
        rabbitTemplate.setReplyTimeout(rabbitReplyTimeout);
        exchange = topicExchange;
        authManager = authMan;
    }

    /**
     * This method gets a ResourceInfo object form repository
     *
     * @param keyParams keys where to look for 'id'
     * @return resource info
     */
    public DbResourceInfo getResourceInfo(List<UriParameter> keyParams) {
        DbResourceInfo resInfo = null;
        if(keyParams != null && !keyParams.isEmpty()){
            final UriParameter key = keyParams.get(0);
            String keyName = key.getName();
            String keyText = key.getText();
            //remove quote
            keyText = keyText.replaceAll("'", "");
            try {
                if (keyName.equalsIgnoreCase("id")) {
                    Optional<DbResourceInfo> resInfoOptional = resourcesRepo.findById(keyText);
                    if (resInfoOptional.isPresent()) {
                        resInfo = resInfoOptional.get();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return resInfo;
    }

    /**
     * This method is used to get object from OData query
     *
     * @param resourceInfoList  the list of resourceInfo objects
     * @param top               the top parameter of the OData query
     * @param filterQuery       the filter parameter of the OData query
     * @return                  RapPluginResponse object
     * @throws ODataApplicationException exception in handling OData
     */
    public RapPluginResponse getRelatedObject(List<ResourceInfo> resourceInfoList, Integer top, Query filterQuery) throws ODataApplicationException {
        String symbioteId = null;
        RapPluginResponse response = null;
        System.out.println("StorageHelper getRelatedObject");
        try {
            top = (top == null) ? TOP_LIMIT : top;
            ResourceAccessMessage msg;
            
            String pluginId = null;
            for(ResourceInfo resourceInfo: resourceInfoList){
                String symbioteIdTemp = resourceInfo.getSymbioteId();
                if(symbioteIdTemp != null && !symbioteIdTemp.isEmpty())
                    symbioteId = symbioteIdTemp;
                String pluginIdTemp = resourceInfo.getPluginId();
                if(pluginIdTemp != null && !pluginIdTemp.isEmpty())
                    pluginId = pluginIdTemp;
            }

            // set default plugin if only one plugin registered in RAP
            if(pluginId == null) {
                if(pluginRepo.count() != 1)
                    throw new ODataApplicationException("No plugin found for specified resource", 
                            HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT);
                
                List<PlatformInfo> lst = pluginRepo.findAll();
                pluginId = lst.get(0).getPlatformId();
            }        

            String routingKey;
            if (top == 1) {
                msg = new ResourceAccessGetMessage(resourceInfoList);
                routingKey =  pluginId + "." + ResourceAccessMessage.AccessType.GET.toString().toLowerCase();
            } else {
                msg = new ResourceAccessHistoryMessage(resourceInfoList, top, filterQuery);
                routingKey =  pluginId + "." + ResourceAccessMessage.AccessType.HISTORY.toString().toLowerCase();
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            String json = mapper.writeValueAsString(msg);

            log.debug("Message: ");
            log.debug(json);
            Object obj = rabbitTemplate.convertSendAndReceive(exchange.getName(), routingKey, json);
            response = extractRapPluginResponse(obj);
            
            if(response instanceof RapPluginOkResponse) {
                RapPluginOkResponse okResponse = (RapPluginOkResponse) response;
                if(okResponse.getBody() != null) {
                    try {
                        // need to clean up response if top 1 is used and RAP plugin does not support filtering
                        if (top == 1) {
                            reduceTop1(okResponse, symbioteId);
                        } else { 
                            // top is not 1
                            reduceTopN(top, okResponse, symbioteId);
                            /*
                            // THIS WOULD CUT OUT SUPPORT FOR PIMs
                            else {
                                throw new IllegalStateException("Unsupported body response form RAP plugin. Expected observation list but got " + okResponse.getBody().getClass().getName());
                            }
                            */
                        }
                    } catch (Exception e) {
                        throw new ODataApplicationException("Can not parse returned object from RAP plugin.\nCause: " + e.getMessage(),
                                HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
                                Locale.ROOT,
                                e);
                    }
                }
            } else {
                RapPluginErrorResponse errorResponse = (RapPluginErrorResponse) response;
                throw new ODataApplicationException(errorResponse.getMessage(), errorResponse.getResponseCode(), null);
            }
            
            return response;
        } catch (ODataApplicationException ae) {
            throw ae;
        } catch (Exception e) {
            String err = "Unable to read resource " + symbioteId;
            err += "\n Error: " + e.getMessage();
            log.error(err);
            throw new ODataApplicationException(err, HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }
    }

    private void reduceTopN(Integer top, RapPluginOkResponse okResponse, String symbioteId) {
        okResponse.bodyToObservations().ifPresent(internalObservations -> {
            
            if(internalObservations.size() != 0) {
                @SuppressWarnings("unchecked")
                
                List<Observation> observationsList = new ArrayList<>();
                int i = 0;
                for (Observation o : internalObservations) {
                    i++;
                    if(i > top) {
                        break;
                    }
                    Observation ob = new Observation(symbioteId, o.getLocation(), o.getResultTime(), o.getSamplingTime(), o.getObsValues());
                    observationsList.add(ob);
                }
                okResponse.updateBody(observationsList);
            }
        });
    }

    private void reduceTop1(RapPluginOkResponse okResponse, String symbioteId) {
        okResponse.bodyToObservations().ifPresent(observations -> {
            if(observations.size() != 0) {
                @SuppressWarnings("unchecked")
                Observation internalObservation = observations.get(0);
                Observation observation = new Observation(symbioteId, internalObservation.getLocation(),
                        internalObservation.getResultTime(), internalObservation.getSamplingTime(),
                        internalObservation.getObsValues());
                okResponse.updateBody(Arrays.asList(observation));
            }
        });
        okResponse.bodyToObservation().ifPresent(internalObservation -> {
            Observation observation = new Observation(symbioteId, internalObservation.getLocation(),
                    internalObservation.getResultTime(), internalObservation.getSamplingTime(),
                    internalObservation.getObsValues());
            okResponse.updateBody(Arrays.asList(observation));
        });
        
//        } else if(okResponse.getBody() instanceof Map) {
//            String jsonBody = mapper.writeValueAsString(okResponse.getBody());
//            try {
//                internalObservation = mapper.readValue(jsonBody, Observation.class);
//                Observation observation = new Observation(symbioteId, internalObservation.getLocation(),
//                        internalObservation.getResultTime(), internalObservation.getSamplingTime(),
//                        internalObservation.getObsValues());
//                okResponse.setBody(Arrays.asList(observation));
//            } catch (Exception e) { /* do nothing*/ }
//        } 
        /*
        // THIS WOULD CUT OUT SUPPORT FOR PIMs
        else {
            throw new IllegalStateException("Unsupported body response form RAP plugin when reading one resource. Got " + 
                    okResponse.getBody().getClass().getName());
        }
        */    
    }

    private RapPluginResponse extractRapPluginResponse(Object obj)
            throws ODataApplicationException, UnsupportedEncodingException {
        ObjectMapper mapper = new ObjectMapper();
        if (obj == null) {
            log.error("No response from plugin");
            throw new ODataApplicationException("No response from plugin", HttpStatusCode.GATEWAY_TIMEOUT.getStatusCode(), Locale.ROOT);
        }

        String rawObj;
        if (obj instanceof byte[]) {
            rawObj = new String((byte[]) obj, "UTF-8");
        } else if (obj instanceof String){
            rawObj = (String) obj;
        } else {
            throw new ODataApplicationException("Can not parse response from RAP plugin. Expected byte[] or String but got " + obj.getClass().getName(), 
                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
                    Locale.ROOT);
        }

        try {
            RapPluginResponse resp = mapper.readValue(rawObj, RapPluginResponse.class);
            String content = resp.getContent();
            if(content != null && content.length() > 0) {
                JsonNode jsonObj = mapper.readTree(content);
                if (!jsonObj.has(JSON_OBJECT_TYPE_FIELD_NAME)) {
                    log.error("Field " + JSON_OBJECT_TYPE_FIELD_NAME + " is mandatory");
                }
            }
            return resp;
        } catch (IOException jpe) {
            // try to map old response
            try {
                JsonNode jsonTree = mapper.readTree(rawObj);
                int responseCode = jsonTree.get("responseCode").asInt();
                String bodyJson = mapper.writeValueAsString(jsonTree.get("body"));
                return RapPluginOkResponse.createFromJson(responseCode, bodyJson);
            } catch (Exception e) {
                throw new ODataApplicationException("Can not parse response from RAP to JSON.\n Cause: " + e.getMessage(),
                    HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), 
                    Locale.ROOT,
                    e);
            }
        }
    }


    /**
     * This method set the value of an object specified in an OData request
     * @param resourceInfoList  the list of ResourceInfo objects
     * @param requestBody       the body of the request
     * @return                  RapPluginResponse object
     * @throws ODataApplicationException exception in handling OData
     */
    public RapPluginResponse setService(List<ResourceInfo> resourceInfoList, String requestBody) throws ODataApplicationException {
        @SuppressWarnings("unused")
        String type = "";
        try {
            ResourceAccessMessage msg;
            String pluginId = null;
            String internalId = null;
            for(ResourceInfo resourceInfo: resourceInfoList){
                pluginId = resourceInfo.getPluginId();
                type = resourceInfo.getType();
                internalId = resourceInfo.getInternalId();
                if(pluginId != null)
                    break;
            }
            if(pluginId == null) {
                List<PlatformInfo> lst = pluginRepo.findAll();
                if(lst == null || lst.isEmpty())
                    throw new Exception("No plugin found");
                
                pluginId = lst.get(0).getPlatformId();
            }
            
            DbResourceInfo dbResourceInfo = resourcesRepo.findByInternalId(internalId).get(0);

            if(isService(dbResourceInfo)) {
                requestBody = "[" + requestBody + "]";
                if(validateServiceRequestPayload)
                    validateServiceRequestBody(dbResourceInfo, requestBody);
            } else { // actuator
                if(validateActuatorRequestPayload)
                    validateActuationRequestBody(dbResourceInfo, requestBody);
            }
            
            String routingKey = pluginId + "." + ResourceAccessMessage.AccessType.SET.toString().toLowerCase();
            
            msg = new ResourceAccessSetMessage(resourceInfoList, requestBody);

            String json = "";
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

                json = mapper.writeValueAsString(msg);
            } catch (JsonProcessingException ex) {
                log.error("JSon processing exception: " + ex.getMessage());
            }
            log.debug("Message Set: " + json);
            Object o = rabbitTemplate.convertSendAndReceive(exchange.getName(), routingKey, json);
            
            RapPluginResponse rpResponse = extractRapPluginResponse(o);
            if(rpResponse instanceof RapPluginErrorResponse) {
                RapPluginErrorResponse errorResponse = (RapPluginErrorResponse) rpResponse;
                throw new ODataApplicationException(errorResponse.getMessage(), errorResponse.getResponseCode(), null);
            }
            
            // validate service response
            if(validateServiceResponsePayload && isService(dbResourceInfo)) {
                validateServiceResponseBody(dbResourceInfo, rpResponse.getContent()); 
            }
            
            return rpResponse;
        } catch (eu.h2020.symbiote.validation.ValidationException ve) {
            throw new ODataApplicationException("Validation error: " + ve.getMessage(), 
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT, ve);
        } catch (ODataApplicationException ae) {
            throw ae;        
        } catch (Exception e) {
            throw new ODataApplicationException("Internal Error", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ROOT, e);
        }
    }

    private boolean isService(DbResourceInfo dbResourceInfo) {
        return dbResourceInfo.getResource() instanceof Service;
    }
    
    private void validateActuationRequestBody(DbResourceInfo dbResourceInfo, String payload) throws eu.h2020.symbiote.validation.ValidationException {
        if(dbResourceInfo.getResource() == null) {
            log.warn("Try to validate request body for actuator with internalId '{}' but registration in mongo doesn't have resource. Please reregister resource.", dbResourceInfo.getInternalId());
            return;
        }
        List<Capability> capabilitiesDefined = ((Actuator)dbResourceInfo.getResource()).getCapabilities();
        ValidationHelper.validateActuatorPayload(capabilitiesDefined, payload);        
    }

    private void validateServiceRequestBody(DbResourceInfo dbResourceInfo, String payload) throws eu.h2020.symbiote.validation.ValidationException {
        if(dbResourceInfo.getResource() == null) {
            log.warn("Try to validate request body for service with internalId '{}' but registration in mongo doesn't have resource. Please reregister resource.", dbResourceInfo.getInternalId());
            return;
        }
        List<Parameter> parametersDefined = ((Service)dbResourceInfo.getResource()).getParameters();
        ValidationHelper.validateServicePayload(parametersDefined, payload);
    }

    private void validateServiceResponseBody(DbResourceInfo dbResourceInfo, String responseBody) throws eu.h2020.symbiote.validation.ValidationException {
        if(dbResourceInfo.getResource() == null) {
            log.warn("Try to validate response body for service with internalId '{}' but registration in mongo doesn't have resource. Please reregister resource.", dbResourceInfo.getInternalId());
            return;
        }
        
        Datatype resultType = ((Service)dbResourceInfo.getResource()).getResultType();
        if(resultType == null) {
            log.warn("Can not validate response body for service with internalId '{}' because service don't have resultType. Please reregister resource with resultType.", dbResourceInfo.getInternalId());            
        }
        
        try {
            ValidationHelper.validateType(resultType, responseBody, null);
        } catch (eu.h2020.symbiote.validation.ValidationException ve) {
            throw new eu.h2020.symbiote.validation.ValidationException("Service response body - " + ve.getMessage(), ve);
        }
    }
    
    /**
     * This method is used to execute a filter locally on RAP
     * @param expression    the filter expression
     * @return              the Query object
     * @throws ODataApplicationException exception in handling OData
     */
    public static Query calculateFilter(Expression expression) throws ODataApplicationException {
        System.out.println("StorageHelper calculateFilter");

        if (expression instanceof Binary) {
            Expression left = ((Binary) expression).getLeftOperand();
            BinaryOperatorKind operator = ((Binary) expression).getOperator();
            Expression right = ((Binary) expression).getRightOperand();

            if (left instanceof Binary && right instanceof Binary) {
                List<Query> exprs = new ArrayList<>();
                Operator op = null;
                try {
                    op = new Operator(operator.name());
                } catch (Exception ex) {
                    throw new ODataApplicationException(ex.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
                }

                Query leftQuery = calculateFilter(left);
                exprs.add(0, leftQuery);

                Query rightQuery = calculateFilter(right);
                exprs.add(1, (Query) rightQuery);

                Filter f = new Filter(op.getLop(), exprs);
                return f;
            } else if (left instanceof Member && right instanceof Literal) {
                Member member = (Member) left;
                String key = member.toString();

                Literal literal = (Literal) right;
                String value = literal.getText();
                if (literal.getType() instanceof EdmString) {
                    value = value.substring(1, value.length() - 1);
                }

                if (key.contains("resultTime") || key.contains("samplingTime")) {
                    Matcher matcher = PATTERN.matcher(value);
                    if (!matcher.matches()) {
                        throw new ODataApplicationException("Data format not correct",
                                HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
                    }
                    value = parseDate(value);

                }

                Comparison cmp;
                try {
                    cmp = new Comparison(operator.name());
                } catch (Exception ex) {
                    throw new ODataApplicationException(ex.getMessage(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
                }

                eu.h2020.symbiote.cloud.model.rap.query.Expression expr = new eu.h2020.symbiote.cloud.model.rap.query.Expression(key, cmp.getCmp(), value);

                return expr;
            } else {
                log.error("Not implemented");
                throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
            }
        }
        return null;
    }

    private static String parseDate(String dateParse) throws ODataApplicationException {

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        dateFormat1.setTimeZone(zoneUTC);
        DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");
        dateFormat2.setTimeZone(zoneUTC);
        DateFormat dateFormat3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        dateFormat3.setTimeZone(zoneUTC);

        dateParse = dateParse.replaceAll("Z", "+00:00");
        Date date = null;
        String parsedData;
        try {
            date = dateFormat3.parse(dateParse);
        } catch (ParseException e3) {
            try {
                date = dateFormat2.parse(dateParse);
            } catch (ParseException e2) {
                try {
                    date = dateFormat.parse(dateParse);
                } catch (ParseException e) {
                    try {
                        date = dateFormat1.parse(dateParse);
                    } catch (ParseException e1) {

                    }
                }
            }
        }

        if (date == null) {
            log.error("Incorrect data format");
            throw new ODataApplicationException("Data format not correct",
                    HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
        }

        parsedData = dateFormat.format(date);
        return parsedData;
    }

    /**
     * This method is used to get the list of ResourceInfo objects related to a set of key predicates
     * @param typeNameList      the type name of the list
     * @param keyPredicates     the list of key predicates
     * @return                  the List of ResourceInfo objects
     * @throws ODataApplicationException exception in handling OData
     */
    public List<DbResourceInfo> getResourceInfoList(List<String> typeNameList, List<UriParameter> keyPredicates) throws ODataApplicationException {
        Boolean noResourceFound = true;
        List<DbResourceInfo> resourceInfoList = new ArrayList<>();
        for(int i = 0; i< typeNameList.size(); i++){
            DbResourceInfo resInfo = new DbResourceInfo();
            resInfo.setType(typeNameList.get(i));
            if(i < keyPredicates.size()){
                UriParameter key = keyPredicates.get(i);
                String keyName = key.getName();
                String keyText = key.getText();
                //remove quote
                keyText = keyText.replaceAll("'", "");

                log.debug("keyName = " + keyName);
                log.debug("keyText = " + keyText);

                try {
                    if (keyName.equalsIgnoreCase("id")) {
                        resInfo.setSymbioteId(keyText);
                        Optional<DbResourceInfo> resInfoOptional = resourcesRepo.findById(keyText);
                        if (resInfoOptional.isPresent()) {
                            noResourceFound = false;
                            resInfo.setInternalId(resInfoOptional.get().getInternalId());
                            resInfo.setPluginId(resInfoOptional.get().getPluginId());
                        }
                    }
                } catch (Exception e) {
                }
            }
            resourceInfoList.add(resInfo);
        }
        if(noResourceFound) {
            log.error("No entity found with id specified in request");
            throw new ODataApplicationException("Entity not found",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }
        return resourceInfoList;
    }

    /**
     * This method is used to check access policies towards AdministrationManager
     * @param request OData request
     * @param resourceId resource id
     * @throws Exception security exception
     */
    public void checkAccessPolicies(ODataRequest request, String resourceId) throws Exception {
        log.debug("Checking access policies for resource " + resourceId);
        Map<String,List<String>> headers = request.getAllHeaders();
            Map<String, String> secHdrs = new HashMap<>();
        for(String key : headers.keySet()) {
            secHdrs.put(key, request.getHeader(key));
            //////////////////////////////////////////////////////////////////////////////////
            log.debug("secHdrs key: " + key );
            log.debug("secHdrs request.getHeader(key): " + request.getHeader(key));
            /////////////////////////////////////////////////////////////////////////////////
        }
        log.debug("Headers: " + secHdrs);
        SecurityRequest securityReq = new SecurityRequest(secHdrs);

        AuthorizationResult result = authManager.checkResourceUrlRequest(resourceId, securityReq);
        log.debug("result.isValidated = " + result.isValidated());
        
        if (!result.isValidated())
            throw new ValidationException("The access policies were not satisfied for resource " + resourceId + ". MSG: " + result.getMessage());
    }


    /**
     * This method is used to send a successful access notification to CRAM
     * @param symbioteId symbiote id
     * @param accessType access type from {@link eu.h2020.symbiote.core.cci.accessNotificationMessages.SuccessfulAccessMessageInfo.AccessType SuccessfulAccessMessageInfo.AccessType}
     */
    public void sendSuccessfulAccessMessage(String symbioteId, String accessType){
        try {
            log.debug("sendSuccessfulAccessMessage for id = " + symbioteId + " and accessType = " + accessType);
            if(accessType == null || accessType.isEmpty())
                accessType = SuccessfulAccessMessageInfo.AccessType.NORMAL.name();
            List<Date> dateList = new ArrayList<>();
            dateList.add(new Date());

            notificationService.addSuccessfulAttempts(symbioteId, dateList, accessType);
            notificationService.sendAccessData();
        }catch(Exception e){
            log.error("Error to send SetSuccessfulAttempts to Monitoring", e);
        }
    }

    public boolean isValidateServiceRequestPayload() {
        return validateServiceRequestPayload;
    }

    public void setValidateServiceRequestPayload(boolean validateServiceRequestPayload) {
        this.validateServiceRequestPayload = validateServiceRequestPayload;
    }

    public boolean isValidateActuatorRequestPayload() {
        return validateActuatorRequestPayload;
    }

    public void setValidateActuatorRequestPayload(boolean validateActuatorRequestPayload) {
        this.validateActuatorRequestPayload = validateActuatorRequestPayload;
    }

    public boolean isValidateServiceResponsePayload() {
        return validateServiceResponsePayload;
    }

    public void setValidateServiceResponsePayload(boolean validateServiceResponsePayload) {
        this.validateServiceResponsePayload = validateServiceResponsePayload;
    }
}
