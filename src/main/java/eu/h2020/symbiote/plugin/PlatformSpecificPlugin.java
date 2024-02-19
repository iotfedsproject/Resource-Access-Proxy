/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import eu.h2020.symbiote.model.cim.WGS84Location;
import eu.h2020.symbiote.model.cim.Observation;
import eu.h2020.symbiote.model.cim.ObservationValue;
import eu.h2020.symbiote.model.cim.Property;
import eu.h2020.symbiote.model.cim.UnitOfMeasurement;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Matteo Pardi
 */
public class PlatformSpecificPlugin extends PlatformPlugin {

    private static final Logger log = LoggerFactory.getLogger(PlatformSpecificPlugin.class);

    private static final boolean PLUGIN_PLATFORM_FILTERS_FLAG = true;
    private static final boolean PLUGIN_PLATFORM_NOTIFICATIONS_FLAG = true;

    public static final String PLUGIN_PLATFORM_ID = "platform_01";
    public static final String PLUGIN_RES_ACCESS_QUEUE = "rap-platform-queue_" + PLUGIN_PLATFORM_ID;


    public PlatformSpecificPlugin(RabbitTemplate rabbitTemplate, TopicExchange exchange) {
        super(rabbitTemplate, exchange, PLUGIN_PLATFORM_ID, PLUGIN_PLATFORM_FILTERS_FLAG, PLUGIN_PLATFORM_NOTIFICATIONS_FLAG);
    }

    /**
     * This is called when received request for reading resource.
     *
     * You need to checked if you can read sensor data with that internal id and in case
     * of problem you can throw RapPluginException
     *
     * @param resourceId internal id of sensor as registered
     *
     * @return string that contains JSON of one Observation
     *
     * @throws RapPluginException can be thrown when something went wrong. It has return code
     * that can be returned to consumer.
     */
    @Override
    public String readResource(String resourceId) {
        String json;
        try {
            //
            // INSERT HERE: query to the platform with internal resource id
            //
            // example
//            if("isen1".equals(resourceId)) { //tropopoihse edw to string

            System.out.println("readResource resourceId = "+ resourceId);

            if(resourceId.contains("isen")) {
                Observation obs = observationExampleValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
              //  throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }

            /*
             * Anthrax level
             */
           /* if(resourceId.equals("sensor10")) {
                System.out.println("Getting observation for Anthrax level");
                Observation obs = observationAnthraxLevel(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } else {
                throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            }*/
            else
            if(resourceId.equals("sensor1")) {
                System.out.println("Getting observation for Carbon Monoxide");
                Observation obs = observationCarbonMonoxideLevel(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            }// else {
             //   throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}
            else
            if(resourceId.equals("sensor2")) {
                System.out.println("Getting observation for Carbon Dioxide");
                Observation obs = observationCarbonDioxideLevel(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }
            else
            if(resourceId.equals("sensor3")) {
                System.out.println("Getting observation for door state 1");
                Observation obs = observationDoorStateValue1(resourceId);//returns opened/closed
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}
            else
            if(resourceId.equals("sensor31")) {
                System.out.println("Getting observation for door state 2");
                Observation obs = observationDoorStateValue2(resourceId);//returns 0/1
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            }// else {
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }
            else
            if(resourceId.equals("sensor4")) {
                System.out.println("Getting observation for air temperature");
                Observation obs = observationTemperatureValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
              //  throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }
            else
            if(resourceId.equals("sensor5")) {
                System.out.println("Getting observation for humidity");
                Observation obs = observationHumidityValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
              //  throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }
            else
            if(resourceId.equals("sensor6")) {
                System.out.println("Getting observation for AtmosphericPressure");
                Observation obs = observationAtmosphericPressureValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
              //  throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
          //  }
            else
            if(resourceId.equals("sensor61")) {
                System.out.println("Getting observation for Meteo station");
                Observation obs = observationMeteoStationValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}
            else
            if(resourceId.equals("sensor7")) {
                System.out.println("Getting observation for RadiationLevel");
                Observation obs = observationRadiationLevelValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            }// else {
              //  throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }


            else
            if(resourceId.equals("sensor8")) {
                System.out.println("Getting observation for Bio Threat A");
                Observation obs = observationBioThreatAValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            }// else {
              //  throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }
            else
            if(resourceId.equals("sensor9")) {
                System.out.println("Getting observation for Bio Threat B");
                Observation obs = observationBioThreatBValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
              //  throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }
            else
            if(resourceId.equals("sensor10")) {
                System.out.println("Getting observation for Bio Threat C");
                Observation obs = observationBioThreatCValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}


            ///
            else
            if(resourceId.equals("sensor11")) {
                System.out.println("Getting observation for RadiationLevel A");
                Observation obs = observationRadiationLevelAValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
                //throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}
            else
            if(resourceId.equals("sensor12")) {
                System.out.println("Getting observation for RadiationLevel B");
                Observation obs = observationRadiationLevelBValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } //else {
              //  throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}
            else
            if(resourceId.equals("sensor13")) {
                System.out.println("Getting observation for RadiationLevel C");
                Observation obs = observationRadiationLevelCValue(resourceId);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(obs);
                return json;
            } else {
                throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            }


        } catch (JsonProcessingException ex) {
            throw new RapPluginException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can not convert to JSON.", ex);
        }
    }//end

    /**
     * This method is called when actuating resource or invoking service is requested.
     *
     * In the case of actuation
     * body will be JSON Object with capabilities and parameters.
     * Actuation does not return value (it will be ignored).
     * Example of body:
     * <pre>
     * {
     *   "SomeCapabililty" : [
     *     {
     *       "param1" : true
     *     },
     *     {
     *       "param2" : "some text"
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * In the case of invoking service body will be JSON Array with parameters.
     * Example of body:
     * <pre>
     * [
     *   {
     *     "inputParam1" : false
     *   },
     *   {
     *     "inputParam2":"some text"
     *   },
     *   ...
     * ]
     * </pre>
     *
     * @param body JSON input depending on what is called (actuation or invoking service)
     *
     * @return returns JSON string that will be returned as response
     *
     * @throws RapPluginException can be thrown when something went wrong. It has return code
     * that can be returned to consumer.
     */
    @Override
    public String writeResource(String resourceId, String body) {
        // INSERT HERE: call to the platform with internal resource id
        String newBody = body.trim();
        if(newBody.charAt(0) == '{') {
            // actuation
            System.out.println("Actuation on resource " + resourceId + " called.");
            if("iaid1".equals(resourceId)) {
                try {
                    // This is example of extracting data from body
                    ObjectMapper mapper = new ObjectMapper();
                    HashMap<String,ArrayList<HashMap<String, Object>>> jsonObject =
                            mapper.readValue(body, new TypeReference<HashMap<String,ArrayList<HashMap<String, Object>>>>() { });
                    for(Entry<String, ArrayList<HashMap<String,Object>>> capabilityEntry: jsonObject.entrySet()) {
                        System.out.println("Found capability " + capabilityEntry.getKey());
                        System.out.println(" There are " + capabilityEntry.getValue().size() + " parameters.");
                        for(HashMap<String, Object> parameterMap: capabilityEntry.getValue()) {
                            for(Entry<String, Object> parameter: parameterMap.entrySet()) {
                                System.out.println(" paramName: " + parameter.getKey());
                                System.out.println(" paramValueType: " + parameter.getValue().getClass().getName() + " value: " + parameter.getValue() + "\n");
                            }
                        }
                    }
                    System.out.println("jsonObject:  " + jsonObject);
                    // actuation always returns null if everything is ok
                    return null;
                } catch (IOException e) {
                    throw new RapPluginException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
                }
            } else {
                throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            }
        } else {
            // invoking service
            System.out.println("Invoking service " + resourceId + ".");
            if("isrid1".equals(resourceId)) {
                try {
                    // extracting service parameters
                    ObjectMapper mapper = new ObjectMapper();
                    ArrayList<HashMap<String, Object>> jsonObject =
                            mapper.readValue(body, new TypeReference<ArrayList<HashMap<String, Object>>>() { });
                    for(HashMap<String,Object> parameters: jsonObject) {
                        System.out.println("Found " + parameters.size() + " parameter(s).");
                        for(Entry<String, Object> parameter: parameters.entrySet()) {
                            System.out.println(" paramName: " + parameter.getKey());
                            System.out.println(" paramValueType: " + parameter.getValue().getClass().getName() + " value: " + parameter.getValue() + "\n");
                        }
                    }
                    System.out.println("jsonObject:  " + jsonObject);
                    // Service can return either null if nothing to return or some JSON
                    // example
                    return "\"some json\"";
                } catch (IOException e) {
                    throw new RapPluginException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
                }
            } else {
                throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Service not found!");
            }
        }
    }

    /**
     * This is called when received request for reading resource history.
     *
     * You need to checked if you can read sensor data with that internal id and in case
     * of problem you can throw RapPluginException.
     *
     * Default is to return maximum of 100 observations.
     *
     * @param resourceId internal id of sensor as registered
     *
     * @return string that contains JSON with array of Observations (maximum 100)
     *
     * @throws RapPluginException can be thrown when something went wrong. It has return code
     * that can be returned to consumer.
     */
    @Override
    public String readResourceHistory(String resourceId) {
        String json;
        try {
            List<Observation> value = new ArrayList<>();
            //
            // INSERT HERE: query to the platform with internal resource id and
            // return list of observations in JSON
            //
            // Here is example
            if("sensor1".equals(resourceId)) {
                Observation obs1 = observationCarbonMonoxideLevel(resourceId);
                Observation obs2 = observationCarbonMonoxideLevel(resourceId);
                Observation obs3 = observationCarbonMonoxideLevel(resourceId);
                Observation obs4 = observationCarbonMonoxideLevel(resourceId);
                Observation obs5 = observationCarbonMonoxideLevel(resourceId);
                Observation obs6 = observationCarbonMonoxideLevel(resourceId);
                Observation obs7 = observationCarbonMonoxideLevel(resourceId);
                Observation obs8 = observationCarbonMonoxideLevel(resourceId);
                Observation obs9 = observationCarbonMonoxideLevel(resourceId);
                Observation obs10 = observationCarbonMonoxideLevel(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else// {
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }
            if("sensor2".equals(resourceId)) {
                Observation obs1 = observationCarbonDioxideLevel(resourceId);
                Observation obs2 = observationCarbonDioxideLevel(resourceId);
                Observation obs3 = observationCarbonDioxideLevel(resourceId);
                Observation obs4 = observationCarbonDioxideLevel(resourceId);
                Observation obs5 = observationCarbonDioxideLevel(resourceId);
                Observation obs6 = observationCarbonDioxideLevel(resourceId);
                Observation obs7 = observationCarbonDioxideLevel(resourceId);
                Observation obs8 = observationCarbonDioxideLevel(resourceId);
                Observation obs9 = observationCarbonDioxideLevel(resourceId);
                Observation obs10 = observationCarbonDioxideLevel(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
          //  }
            if("sensor3".equals(resourceId)) {
                Observation obs1 = observationDoorStateValue1(resourceId);
                Observation obs2 = observationDoorStateValue1(resourceId);
                Observation obs3 = observationDoorStateValue1(resourceId);
                Observation obs4 = observationDoorStateValue1(resourceId);
                Observation obs5 = observationDoorStateValue1(resourceId);
                Observation obs6 = observationDoorStateValue1(resourceId);
                Observation obs7 = observationDoorStateValue1(resourceId);
                Observation obs8 = observationDoorStateValue1(resourceId);
                Observation obs9 = observationDoorStateValue1(resourceId);
                Observation obs10 = observationDoorStateValue1(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
                //throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
          //  }

            if("sensor31".equals(resourceId)) {
                Observation obs1 = observationDoorStateValue2(resourceId);
                Observation obs2 = observationDoorStateValue2(resourceId);
                Observation obs3 = observationDoorStateValue2(resourceId);
                Observation obs4 = observationDoorStateValue2(resourceId);
                Observation obs5 = observationDoorStateValue2(resourceId);
                Observation obs6 = observationDoorStateValue2(resourceId);
                Observation obs7 = observationDoorStateValue2(resourceId);
                Observation obs8 = observationDoorStateValue2(resourceId);
                Observation obs9 = observationDoorStateValue2(resourceId);
                Observation obs10 = observationDoorStateValue2(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
                //throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
          //  }

            if("sensor4".equals(resourceId)) {
                Observation obs1 = observationTemperatureValue(resourceId);
                Observation obs2 = observationTemperatureValue(resourceId);
                Observation obs3 = observationTemperatureValue(resourceId);
                Observation obs4 = observationTemperatureValue(resourceId);
                Observation obs5 = observationTemperatureValue(resourceId);
                Observation obs6 = observationTemperatureValue(resourceId);
                Observation obs7 = observationTemperatureValue(resourceId);
                Observation obs8 = observationTemperatureValue(resourceId);
                Observation obs9 = observationTemperatureValue(resourceId);
                Observation obs10 = observationTemperatureValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
                //throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}

            if("sensor5".equals(resourceId)) {
                Observation obs1 = observationHumidityValue(resourceId);
                Observation obs2 = observationHumidityValue(resourceId);
                Observation obs3 = observationHumidityValue(resourceId);
                Observation obs4 = observationHumidityValue(resourceId);
                Observation obs5 = observationHumidityValue(resourceId);
                Observation obs6 = observationHumidityValue(resourceId);
                Observation obs7 = observationHumidityValue(resourceId);
                Observation obs8 = observationHumidityValue(resourceId);
                Observation obs9 = observationHumidityValue(resourceId);
                Observation obs10 = observationHumidityValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
                //throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}

            if("sensor6".equals(resourceId)) {
                Observation obs1 = observationAtmosphericPressureValue(resourceId);
                Observation obs2 = observationAtmosphericPressureValue(resourceId);
                Observation obs3 = observationAtmosphericPressureValue(resourceId);
                Observation obs4 = observationAtmosphericPressureValue(resourceId);
                Observation obs5 = observationAtmosphericPressureValue(resourceId);
                Observation obs6 = observationAtmosphericPressureValue(resourceId);
                Observation obs7 = observationAtmosphericPressureValue(resourceId);
                Observation obs8 = observationAtmosphericPressureValue(resourceId);
                Observation obs9 = observationAtmosphericPressureValue(resourceId);
                Observation obs10 = observationAtmosphericPressureValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }

            if("sensor61".equals(resourceId)) {
                Observation obs1 = observationMeteoStationValue(resourceId);
                Observation obs2 = observationMeteoStationValue(resourceId);
                Observation obs3 = observationMeteoStationValue(resourceId);
                Observation obs4 = observationMeteoStationValue(resourceId);
                Observation obs5 = observationMeteoStationValue(resourceId);
                Observation obs6 = observationMeteoStationValue(resourceId);
                Observation obs7 = observationMeteoStationValue(resourceId);
                Observation obs8 = observationMeteoStationValue(resourceId);
                Observation obs9 = observationMeteoStationValue(resourceId);
                Observation obs10 = observationMeteoStationValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}

            if("sensor7".equals(resourceId)) {
                Observation obs1 = observationRadiationLevelValue(resourceId);
                Observation obs2 = observationRadiationLevelValue(resourceId);
                Observation obs3 = observationRadiationLevelValue(resourceId);
                Observation obs4 = observationRadiationLevelValue(resourceId);
                Observation obs5 = observationRadiationLevelValue(resourceId);
                Observation obs6 = observationRadiationLevelValue(resourceId);
                Observation obs7 = observationRadiationLevelValue(resourceId);
                Observation obs8 = observationRadiationLevelValue(resourceId);
                Observation obs9 = observationRadiationLevelValue(resourceId);
                Observation obs10 = observationRadiationLevelValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
                //throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
          //  }

            if("sensor8".equals(resourceId)) {
                Observation obs1 = observationBioThreatAValue(resourceId);
                Observation obs2 = observationBioThreatAValue(resourceId);
                Observation obs3 = observationBioThreatAValue(resourceId);
                Observation obs4 = observationBioThreatAValue(resourceId);
                Observation obs5 = observationBioThreatAValue(resourceId);
                Observation obs6 = observationBioThreatAValue(resourceId);
                Observation obs7 = observationBioThreatAValue(resourceId);
                Observation obs8 = observationBioThreatAValue(resourceId);
                Observation obs9 = observationBioThreatAValue(resourceId);
                Observation obs10 = observationBioThreatAValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else // {
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}

            if("sensor9".equals(resourceId)) {
                Observation obs1 = observationBioThreatBValue(resourceId);
                Observation obs2 = observationBioThreatBValue(resourceId);
                Observation obs3 = observationBioThreatBValue(resourceId);
                Observation obs4 = observationBioThreatBValue(resourceId);
                Observation obs5 = observationBioThreatBValue(resourceId);
                Observation obs6 = observationBioThreatBValue(resourceId);
                Observation obs7 = observationBioThreatBValue(resourceId);
                Observation obs8 = observationBioThreatBValue(resourceId);
                Observation obs9 = observationBioThreatBValue(resourceId);
                Observation obs10 = observationBioThreatBValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
              //  throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }

            if("sensor10".equals(resourceId)) {
                Observation obs1 = observationBioThreatCValue(resourceId);
                Observation obs2 = observationBioThreatCValue(resourceId);
                Observation obs3 = observationBioThreatCValue(resourceId);
                Observation obs4 = observationBioThreatCValue(resourceId);
                Observation obs5 = observationBioThreatCValue(resourceId);
                Observation obs6 = observationBioThreatCValue(resourceId);
                Observation obs7 = observationBioThreatCValue(resourceId);
                Observation obs8 = observationBioThreatCValue(resourceId);
                Observation obs9 = observationBioThreatCValue(resourceId);
                Observation obs10 = observationBioThreatCValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
               // throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }

            if("sensor11".equals(resourceId)) {
                Observation obs1 = observationRadiationLevelAValue(resourceId);
                Observation obs2 = observationRadiationLevelAValue(resourceId);
                Observation obs3 = observationRadiationLevelAValue(resourceId);
                Observation obs4 = observationRadiationLevelAValue(resourceId);
                Observation obs5 = observationRadiationLevelAValue(resourceId);
                Observation obs6 = observationRadiationLevelAValue(resourceId);
                Observation obs7 = observationRadiationLevelAValue(resourceId);
                Observation obs8 = observationRadiationLevelAValue(resourceId);
                Observation obs9 = observationRadiationLevelAValue(resourceId);
                Observation obs10 = observationRadiationLevelAValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else// {
              //  throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
           // }

            if("sensor12".equals(resourceId)) {
                Observation obs1 = observationRadiationLevelBValue(resourceId);
                Observation obs2 = observationRadiationLevelBValue(resourceId);
                Observation obs3 = observationRadiationLevelBValue(resourceId);
                Observation obs4 = observationRadiationLevelBValue(resourceId);
                Observation obs5 = observationRadiationLevelBValue(resourceId);
                Observation obs6 = observationRadiationLevelBValue(resourceId);
                Observation obs7 = observationRadiationLevelBValue(resourceId);
                Observation obs8 = observationRadiationLevelBValue(resourceId);
                Observation obs9 = observationRadiationLevelBValue(resourceId);
                Observation obs10 = observationRadiationLevelBValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else //{
                //throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            //}

            if("sensor13".equals(resourceId)) {
                Observation obs1 = observationRadiationLevelCValue(resourceId);
                Observation obs2 = observationRadiationLevelCValue(resourceId);
                Observation obs3 = observationRadiationLevelCValue(resourceId);
                Observation obs4 = observationRadiationLevelCValue(resourceId);
                Observation obs5 = observationRadiationLevelCValue(resourceId);
                Observation obs6 = observationRadiationLevelCValue(resourceId);
                Observation obs7 = observationRadiationLevelCValue(resourceId);
                Observation obs8 = observationRadiationLevelCValue(resourceId);
                Observation obs9 = observationRadiationLevelCValue(resourceId);
                Observation obs10 = observationRadiationLevelCValue(resourceId);
                value.add(obs1);
                value.add(obs2);
                value.add(obs3);
                value.add(obs4);
                value.add(obs5);
                value.add(obs6);
                value.add(obs7);
                value.add(obs8);
                value.add(obs9);
                value.add(obs10);

                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(value);
                return json;
            } else {
                throw new RapPluginException(HttpStatus.NOT_FOUND.value(), "Sensor not found.");
            }
        } catch (RapPluginException e) {
            throw e;
        } catch (Exception ex) {
            throw new RapPluginException(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex);
        }
    }

    @Override
    public void subscribeResource(String resourceId) {
        // INSERT HERE: call to the platform to subscribe resource
    }

    @Override
    public void unsubscribeResource(String resourceId) {
        // INSERT HERE: call to the platform to unsubscribe resource
    }

    /*
     *   Some sample code for creating one observation
     */
    public Observation observationExampleValue (String resourceId) {
//        String sensorId = "symbIoTeID1";
        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("New York");
        WGS84Location loc = new WGS84Location(2.349014, 48.864716, 15, "New York", ldescr);
        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        //timestamp == resulting Time
        //String timestamp = dateFormat.format(date);
        String timestamp = "2021-09-22T13:20:00-08:00" ;
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        //samplet == sampling time
        //String samplet = dateFormat.format(date);
        String samplet = "2021-09-22T13:20:00-05:00";

        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Blood Glucoce");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("mg/dL");
        ObservationValue obsval = new ObservationValue("120", new Property("Blood Glucose", "GlucoIRI", pdescr),
                new UnitOfMeasurement("mg/dL", "Milligrams per decilitre", "MercuryIri", umdescr));
        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
//----------------------------------------------------------------------------------------------------------------------------------
    //internal id = sensor1 Location Rome rail way station
    public Observation observationCarbonMonoxideLevel (String resourceId) {

     String sensorId = resourceId;
     ArrayList<String> ldescr = new ArrayList<>();
     ldescr.add("Rome Railway station");
     WGS84Location loc = new WGS84Location(12.501164662, 41.900496398, 15, "Rome", ldescr);

     TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
     DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
     dateFormat.setTimeZone(zoneUTC);
     Date date = new Date();
     String timestamp = dateFormat.format(date);
     long ms = date.getTime() - 1000;
     date.setTime(ms);
     String samplet = dateFormat.format(date);
     /*
      * Property assignment
      */
     ArrayList<String> pdescr = new ArrayList<>();
     pdescr.add("Carbon Monoxide (CO) level");
     ArrayList<String> umdescr = new ArrayList<>();
     umdescr.add("parts per million");
     Random r = new Random();
     int low  = 50;
     int high = 200;
     int coValue = r.nextInt(high-low) + low;

     ObservationValue obsval = new ObservationValue(Integer.toString(coValue), new Property("carbonMonoxideConcentration", "carbonMonoxideConcentrationIRI", pdescr),
                new UnitOfMeasurement("ppm", "partsPerMillion", "partsPerMillionIri", umdescr));
     ArrayList<ObservationValue> obsList = new ArrayList<>();
     obsList.add(obsval);
     Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

     log.debug("Observation: \n" + obs.toString());

     return obs;
    }//end
//---------------------------------------------------------------------------------------------------------------------
    //internal id = sensor2 Location Rome railway station
    public Observation observationCarbonDioxideLevel (String resourceId) {

        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Rome Railway station");
        WGS84Location loc = new WGS84Location(12.501164662, 41.900496398, 15, "Rome", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Carbon Dioxide (CO2) level");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("parts per million");
        Random r = new Random();
        int low  = 400;
        int high = 1000;
        int co2Value = r.nextInt(high-low) + low;

        ObservationValue obsval = new ObservationValue(Integer.toString(co2Value), new Property("carbonDioxideConcentration", "carbonDioxideConcentrationIRI", pdescr),
                new UnitOfMeasurement("ppm", "partsPerMillion", "partsPerMillionIri", umdescr));
        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//endobservationDoorStateValue1
//----------------------------------------------------------------------------------------------------------------------
//internal id = sensor3 returns opened/closed Location: Rome railway station
public Observation observationDoorStateValue1 (String resourceId) {

    String sensorId = resourceId;
    ArrayList<String> ldescr = new ArrayList<>();
    ldescr.add("Rome Railway station");
    WGS84Location loc = new WGS84Location(12.501164662, 41.900496398, 15, "Rome", ldescr);

    TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setTimeZone(zoneUTC);
    Date date = new Date();
    String timestamp = dateFormat.format(date);
    long ms = date.getTime() - 1000;
    date.setTime(ms);
    String samplet = dateFormat.format(date);
    /*
     * Property assignment
     */
    ArrayList<String> pdescr = new ArrayList<>();
    pdescr.add("Door state");
    ArrayList<String> umdescr = new ArrayList<>();
    umdescr.add("-");
    Random r = new Random();
    int low  = 0;
    int high = 10;
    int doorStateValue = r.nextInt(high-low) + low;

    String doorState = "closed";
    if(doorStateValue > 5)
        doorState = "opened";

    ObservationValue obsval = new ObservationValue(doorState, new Property("doorState", "doorStateIRI", pdescr),
            new UnitOfMeasurement("-", "-", "-", umdescr));

    ArrayList<ObservationValue> obsList = new ArrayList<>();
    obsList.add(obsval);
    Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

    log.debug("Observation: \n" + obs.toString());

    return obs;
}//end
    //----------------------------------------------------------------------------------------------------------------------
//internal id = sensor31 returns 0/1, Location: Rome railway station
    public Observation observationDoorStateValue2 (String resourceId) {

        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Rome Railway station");
        WGS84Location loc = new WGS84Location(12.501164662, 41.900496398, 15, "Rome", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Door state sensor");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("-");
        Random r = new Random();
        int low  = 0;
        int high = 10;
        int doorStateValue = r.nextInt(high-low) + low;

        int doorState = 0;
        if(doorStateValue > 5)
            doorState = 1;

        ObservationValue obsval = new ObservationValue(Integer.toString(doorState), new Property("doorState", "doorStateIRI", pdescr),
                new UnitOfMeasurement("-", "-", "-", umdescr));

        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
//---------------------------------------------------------------------------------------------------------------
//TO DO
    public Observation observationAnthraxLevel (String resourceId) {

        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Rome Railway station");
        WGS84Location loc = new WGS84Location(12.5016, 41.9013, 15, "Rome", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Anthrax level");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("Becquerels");
        ObservationValue obsval = new ObservationValue("0", new Property("anthrax_level", "anthrax_levelIRI", pdescr),
                new UnitOfMeasurement("Becquerels", "Becquerels", "BecquerelsIri", umdescr));
        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
 //---------------------------------------------------------------------------------------------------------------
 //internal id = sensor4, Location Milano
public Observation observationTemperatureValue (String resourceId) {
    String sensorId = resourceId;
    ArrayList<String> ldescr = new ArrayList<>();
    ldescr.add("Milano Central Railway station");
    WGS84Location loc = new WGS84Location(9.202165858, 45.48499806, 15, "Milano", ldescr);

    TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setTimeZone(zoneUTC);
    Date date = new Date();
    String timestamp = dateFormat.format(date);
    long ms = date.getTime() - 1000;
    date.setTime(ms);
    String samplet = dateFormat.format(date);
    /*
     * Property assignment
     */
    ArrayList<String> pdescr = new ArrayList<>();
    pdescr.add("Air temperature");
    ArrayList<String> umdescr = new ArrayList<>();
    umdescr.add("Temperature in degree Celsius");
    Random r = new Random();
    int low  = 30;
    int high = 40;
    int temperature = r.nextInt(high-low) + low; 
    ObservationValue obsval = new ObservationValue(Integer.toString(temperature), new Property("temperature", "TempIRI", pdescr),
            new UnitOfMeasurement("C", "degree Celsius", "CelsiusIRI", umdescr));
    ArrayList<ObservationValue> obsList = new ArrayList<>();
    obsList.add(obsval);
    Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

    log.debug("Observation: \n" + obs.toString());

    return obs;
}//end
//----------------------------------------------------------------------------------------------------
    //internal id = sensor5, Location: Milano
    public Observation observationHumidityValue (String resourceId) {
        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Milano Central Railway station");
        WGS84Location loc = new WGS84Location(9.202165858, 45.48499806, 15, "Milano", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);

        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Humidity");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("Humidity percentage");
        Random r = new Random();
        int low  = 60;
        int high = 100;
        int humidity = r.nextInt(high-low) + low;
        ObservationValue obsval = new ObservationValue(Integer.toString(humidity), new Property("humidity", "humidityIRI", pdescr),
                new UnitOfMeasurement("%", "percentage", "percentageIRI", umdescr));
        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
//----------------------------------------------------------------------------------------------------------------------
    //internal id = sensor6, Location: Milano central railway station
    public Observation observationAtmosphericPressureValue (String resourceId) {
        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Milano Central Railway station");
        WGS84Location loc = new WGS84Location(9.202165858, 45.48499806, 15, "Milano", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("AtmosphericPressure");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("AtmosphericPressure millibar");
        Random r = new Random();
        int low  = 998;
        int high = 1010;
        int atmosphericPressure = r.nextInt(high-low) + low;
        ObservationValue obsval = new ObservationValue(Integer.toString(atmosphericPressure), new Property("atmosphericPressure", "atmosphericPressureIRI", pdescr),
                new UnitOfMeasurement("mBar", "millibar", "millibarIRI", umdescr));
        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
//------------------------------------------------------------------------------------------------------------------------------
    //internal id = sensor61, Location Paris relway station
    public Observation observationMeteoStationValue (String resourceId) {
        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Paris Railway station");
        WGS84Location loc = new WGS84Location(2.37083185, 48.840163306 , 15, "Paris", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);

        /*
         * Properties assignment
         */

        ArrayList<ObservationValue> obsList = new ArrayList<>();

        /*
         * Atmospheric pressure
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("AtmosphericPressure");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("AtmosphericPressure millibar");
        Random r = new Random();
        int low  = 998;
        int high = 1010;
        int atmosphericPressure = r.nextInt(high-low) + low;
        ObservationValue obsval = new ObservationValue(Integer.toString(atmosphericPressure), new Property("atmosphericPressure", "atmosphericPressureIRI", pdescr),
                new UnitOfMeasurement("mBar", "millibar", "millibarIRI", umdescr));

        obsList.add(obsval);

        /*
         * Temperature
         */

        pdescr = new ArrayList<>();
        pdescr.add("Air temperature");
        umdescr = new ArrayList<>();
        umdescr.add("Temperature in degree Celsius");
        r = new Random();
        low  = 30;
        high = 40;
        int temperature = r.nextInt(high-low) + low;
        obsval = new ObservationValue(Integer.toString(temperature), new Property("temperature", "TempIRI", pdescr),
                new UnitOfMeasurement("C", "degree Celsius", "CelsiusIRI", umdescr));

        obsList.add(obsval);

        /*
         * Humidity
         */

        pdescr.add("Humidity");
        umdescr = new ArrayList<>();
        umdescr.add("Humidity percentage");
        r = new Random();
        low  = 60;
        high = 100;
        int humidity = r.nextInt(high-low) + low;
        obsval = new ObservationValue(Integer.toString(humidity), new Property("humidity", "humidityIRI", pdescr),
                new UnitOfMeasurement("%", "percentage", "percentageIRI", umdescr));
        obsList = new ArrayList<>();
        obsList.add(obsval);


        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
//------------------------------------------------------------------------------------------------------------------------------
    //internal id = sensor7, Location Paris Railway station
    public Observation observationRadiationLevelValue (String resourceId) {
        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Paris Railway station");
        WGS84Location loc = new WGS84Location(2.37083185, 48.840163306 , 15, "Paris", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Radiation Level");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("Radiation Level ");
        Random r = new Random();
        int low  = 998;
        int high = 1010;
        int radiationLevel = r.nextInt(high-low) + low;
        ObservationValue obsval = new ObservationValue(Integer.toString(radiationLevel), new Property("radiationLevel", "radiationLevelIRI", pdescr),
                new UnitOfMeasurement("mBar", "millibar", "millibarIRI", umdescr));
        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
     //-----------------------------------------------------------------------------------------------
    //internal id = sensor8 returns yes/no , Location Paris railway station
    public Observation observationBioThreatAValue (String resourceId) {

        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Paris Railway station");
        WGS84Location loc = new WGS84Location(2.37083185, 48.840163306 , 15, "Paris", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Bio Threat A sensor");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("-");
        Random r = new Random();
        int low  = 0;
        int high = 10;
        int threatValue = r.nextInt(high-low) + low;

        String threat = "Anthrax";
        if(threatValue > 5)
            threat = "no";

        ObservationValue obsval = new ObservationValue(threat, new Property("BioThreatA", "threatIRI", pdescr),
                new UnitOfMeasurement("-", "-", "-", umdescr));

        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
    //-----------------------------------------------------------------------------------------------
    //internal id = sensor9 returns yes/no,Location Paris railway station
    public Observation observationBioThreatBValue (String resourceId) {

        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Paris Railway station");
        WGS84Location loc = new WGS84Location(2.37083185, 48.840163306 , 15, "Paris", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Bio Threat B sensor");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("-");
        Random r = new Random();
        int low  = 0;
        int high = 10;
        int threatValue = r.nextInt(high-low) + low;

        String threat = "Brucellosis ";
        if(threatValue > 5)
            threat = "no";

        ObservationValue obsval = new ObservationValue(threat, new Property("BioThreatB", "threatIRI", pdescr),
                new UnitOfMeasurement("-", "-", "-", umdescr));

        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
//-----------------------------------------------------------------------------------------------------------------------
    //internal id = sensor10 returns yes/no, Location Paris
    public Observation observationBioThreatCValue (String resourceId) {

        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Paris Railway station");
        WGS84Location loc = new WGS84Location(2.37083185, 48.840163306 , 15, "Paris", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Bio Threat C sensor");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("-");
        Random r = new Random();
        int low  = 0;
        int high = 10;
        int threatValue = r.nextInt(high-low) + low;

        String threat = "hantavirus";
        if(threatValue > 5)
            threat = "no";

        ObservationValue obsval = new ObservationValue(threat, new Property("BioThreatC", "threatIRI", pdescr),
                new UnitOfMeasurement("-", "-", "-", umdescr));

        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
//-------------------------------------------------------------------------------------------------------------------------------
    //internal id = sensor11 returns 0, Location Rome
    public Observation observationRadiationLevelAValue (String resourceId) {

        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Rome Railway station");
        WGS84Location loc = new WGS84Location(12.501164662, 41.900496398, 15, "Rome", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Radiation Level A sensor");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("-");
        Random r = new Random();
        int low  = 0;
        int high = 10;
        int threatValue = r.nextInt(high-low) + low;

        String threat = "yes";
        if(threatValue > 5)
            threat = "no";

        ObservationValue obsval = new ObservationValue("0", new Property("RadiationLevelA", "RadiationLevelIRI", pdescr),
                new UnitOfMeasurement("Ci", "curie", "curieIRI", umdescr));

        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
//---------------------------------------------------------------------------------------------------
    //internal id = sensor12 returns 0, Location : Rome
    public Observation observationRadiationLevelBValue (String resourceId) {

        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Rome Railway station");
        WGS84Location loc = new WGS84Location(12.501164662, 41.900496398, 15, "Rome", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Radiation Level B sensor");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("-");
        Random r = new Random();
        int low  = 0;
        int high = 10;
        int threatValue = r.nextInt(high-low) + low;

        String threat = "yes";
        if(threatValue > 5)
            threat = "no";

        ObservationValue obsval = new ObservationValue("0", new Property("RadiationLevelB", "RadiationLevelIRI", pdescr),
                new UnitOfMeasurement("Ci", "curie", "curieIRI", umdescr));

        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end

    //---------------------------------------------------------------------------------------------------
    //internal id = sensor13 returns, Location: Rome , returns 0
    public Observation observationRadiationLevelCValue (String resourceId) {

        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Rome Railway station");
        WGS84Location loc = new WGS84Location(12.501164662, 41.900496398, 15, "Rome", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("Radiation Level C sensor");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("-");
        Random r = new Random();
        int low  = 0;
        int high = 10;
        int threatValue = r.nextInt(high-low) + low;

        String threat = "yes";
        if(threatValue > 5)
            threat = "no";

        ObservationValue obsval = new ObservationValue("0", new Property("RadiationLevelC", "RadiationLevelIRI", pdescr),
                new UnitOfMeasurement("Ci", "curie", "curieIRI", umdescr));

        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end

    //----------------------------------------------------------------------------------------
public Observation observationSensor1Value (String resourceId) {

    String sensorId = resourceId;
    ArrayList<String> ldescr = new ArrayList<>();
    ldescr.add("Rome Railway station");
    WGS84Location loc = new WGS84Location(12.5016, 41.9013, 15, "Rome Railway station", ldescr);
    /*TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setTimeZone(zoneUTC);
    Date date = new Date();
    //timestamp == resulting Time
    //String timestamp = dateFormat.format(date);
    String timestamp = "2021-09-22T13:20:00-08:00" ;
    long ms = date.getTime() - 1000;
    date.setTime(ms);
    //samplet == sampling time
    //String samplet = dateFormat.format(date);
    String samplet = "2021-09-22T13:20:00-05:00";*/
    TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setTimeZone(zoneUTC);
    Date date = new Date();
    String timestamp = dateFormat.format(date);
    long ms = date.getTime() - 1000;
    date.setTime(ms);
    String samplet = dateFormat.format(date);
    /*
     * Property assignment
     */
    ArrayList<String> pdescr = new ArrayList<>();
    pdescr.add("radioactivity");
    ArrayList<String> umdescr = new ArrayList<>();
    umdescr.add("Becquerels");
    ObservationValue obsval = new ObservationValue("0", new Property("radioactivity", "BecquerelsIRI", pdescr),
            new UnitOfMeasurement("Becquerels", "Becquerels", "BecquerelsIri", umdescr));
    ArrayList<ObservationValue> obsList = new ArrayList<>();
    obsList.add(obsval);
    Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

    log.debug("Observation: \n" + obs.toString());

    return obs;
}//end
    //------------------------------------------------------------------------------------------------------------------------------
    public Observation observationSensor2Value (String resourceId) {

        String sensorId = resourceId;
        ArrayList<String> ldescr = new ArrayList<>();
        ldescr.add("Rome Railway station");
        WGS84Location loc = new WGS84Location(12.5016, 41.9013, 15, "Rome Railway station", ldescr);

        TimeZone zoneUTC = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormat.setTimeZone(zoneUTC);
        Date date = new Date();
        String timestamp = dateFormat.format(date);
        long ms = date.getTime() - 1000;
        date.setTime(ms);
        String samplet = dateFormat.format(date);
        /*
         * Property assignment
         */
        ArrayList<String> pdescr = new ArrayList<>();
        pdescr.add("nuclearEnergy");
        ArrayList<String> umdescr = new ArrayList<>();
        umdescr.add("Becquerels");
        ObservationValue obsval = new ObservationValue("0", new Property("nuclearEnergy", "BecquerelsIRI", pdescr),
                new UnitOfMeasurement("Becquerels", "Becquerels", "BecquerelsIri", umdescr));
        ArrayList<ObservationValue> obsList = new ArrayList<>();
        obsList.add(obsval);
        Observation obs = new Observation(sensorId, loc, timestamp, samplet , obsList);

        log.debug("Observation: \n" + obs.toString());

        return obs;
    }//end
}//end of class
