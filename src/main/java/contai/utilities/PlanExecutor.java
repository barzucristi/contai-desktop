package contai.utilities;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import contai.service.RestCloudActionService;
import contai.service.RestPlanService;
import org.apache.log4j.Logger;


public class PlanExecutor {
    private static final Logger logger = Logger.getLogger(PlanExecutor.class);

    private Timer timer;
    private String authToken;
    private JsonObject userData;
    private JsonArray cuiData;
    private String storedId;

    public PlanExecutor(String authToken, JsonObject userData, JsonObject cui) {
        this.authToken = authToken;
        this.userData = userData;
        this.timer = new Timer();
        this.cuiData = new JsonArray();

        if (cui != null && cui.has("active") && cui.get("active").isJsonArray()) {
            JsonArray activeArray = cui.getAsJsonArray("active");
            for (JsonElement element : activeArray) {
                if (element.isJsonPrimitive()) {
                    cuiData.add(element);
                }
            }
        }
    }


    public void setupPlans() {
        if (userData != null && userData.has("current_subscription")) {
            JsonObject subscription = userData.getAsJsonObject("current_subscription");
            if (subscription.has("stripe_plan")) {
                JsonObject stripePlan = subscription.getAsJsonObject("stripe_plan");
                if (stripePlan.has("desktop_plans")) {
                    JsonArray desktopPlans = stripePlan.getAsJsonArray("desktop_plans");
                    for (JsonElement planElement : desktopPlans) {
                        JsonObject plan = planElement.getAsJsonObject();
                        if (plan.get("active").getAsBoolean()) {
                            setupPlanTrigger(plan);
                        }
                    }
                }
            }
        }
    }

    private void setupPlanTrigger(JsonObject plan) {
 	   String type = plan.has("frequency_type") && !plan.get("frequency_type").isJsonNull() 
 	            ? plan.get("frequency_type").getAsString() 
 	            : "unknown";

 	   Integer value = null;

 	    // Handle the frequency value safely
 	    if (plan.has("frequency_value") && !plan.get("frequency_value").isJsonNull()) {
 	        try {
 	            value = plan.get("frequency_value").getAsInt();
 	        } catch (UnsupportedOperationException e) {
 	            logger.info("Frequency value is not an integer for plan: " + plan.get("name").getAsString());
 	        }
 	    }

     // Log frequency type and value for debugging
//     logger.info("Triggering plan: " + plan.get("name").getAsString() + " at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
//     logger.info("Frequency Type: " + type);
     if (value != null) {
//     	logger.info("Frequency Value: " + value);
     } else {
     	logger.info("Frequency Value is not available for plan: " + plan.get("name").getAsString());
     }

     // Handle different frequency types
     switch (type) {
         case "minutes":
             if (value != null) {
                 long interval = value * 60 * 1000; // Convert minutes to milliseconds
                 timer.scheduleAtFixedRate(new TimerTask() {
                     @Override
                     public void run() {
                         triggerPlan(plan);
                     }
                 }, 0, interval);
             } else {
             	logger.info("Cannot schedule plan with null frequency value.");
             }
             break;
         case "always_on":
             triggerPlan(plan);
             timer.scheduleAtFixedRate(new TimerTask() {
                 @Override
                 public void run() {
                     triggerPlan(plan);
                 }
             }, 0, 5 * 60 * 1000); // Every 5 minutes
             break;
         default:
         	logger.info("Unknown frequency type: " + type);
     }
 }


    private void triggerPlan(JsonObject plan) {
        String planName = plan.get("name").getAsString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(new Date());
        logger.info("Triggering plan: " + planName + " at " + currentTime);

        // Directly access frequency_type and frequency_value
        String frequencyType = plan.has("frequency_type") && !plan.get("frequency_type").isJsonNull() 
                ? plan.get("frequency_type").getAsString() 
                : "N/A"; // Default value if 'type' is null

        // Initialize frequencyValue as null and handle cases where "value" may be missing or null
        Integer frequencyValue = null;
        if (plan.has("frequency_value") && !plan.get("frequency_value").isJsonNull()) {
            try {
                frequencyValue = plan.get("frequency_value").getAsInt();
            } catch (UnsupportedOperationException e) {
                logger.warn("Frequency value is not an integer for plan: " + planName);
            }
        }

        logger.info("Frequency Type: " + frequencyType);
        if (frequencyValue != null) {
            logger.info("Frequency Value: " + frequencyValue);
        } else {
            logger.info("Frequency Value is not available for plan: " + planName);
        }

        executePlanActions(planName, frequencyType, frequencyValue);
    }




    private void executePlanActions(String planName, String frequencyType, Integer frequencyValue) {
        switch (planName) {
            case "Plan A":
               	logger.info("Executing actions for Plan A with frequency type: " + frequencyType + " and value: " + frequencyValue);
            	processPlanA();
                break;
            case "Plan B":
            	processPlanB();
                // Trigger specific actions for Plan B
            	logger.info("Executing actions for Plan B with frequency type: " + frequencyType + " and value: " + frequencyValue);
                break;
            case "Plan C":
            	processPlanC();
                // Trigger specific actions for Plan C
            	logger.info("Executing actions for Plan C with frequency type: " + frequencyType + " and value: " + frequencyValue);
                break;
            case "Plan D":
            	processPlanD(); 
                // Trigger specific actions for Plan D
            	logger.info("Executing actions for Plan D with frequency type: " + frequencyType + " and value: " + frequencyValue);
                break;
            // Add cases for other plans as needed
            default:
//            	logger.info("Unknown plan: " + planName);
                break;
        }
    }

    private void processPlanA() {
        try {
            Set<String> currentIterationCuiSet = new HashSet<>();
            Set<String> currentCuiSet = new HashSet<>();

            // First, use existing CuiData
            logger.info("CuiData response---"+cuiData);
           
            if (cuiData != null) {
                for (JsonElement element : cuiData) {
                    if (element.isJsonPrimitive()) {
                        String cui = element.getAsString();
                        currentCuiSet.add(cui.trim());
                    }
                }
            }

            
      
            // Then, check from API
            JsonObject response = RestPlanService.executePlan("https://webserviced.anaf.ro/SPVWS2/rest/listaMesaje?zile=1");               
            String cuiString = response.get("cui").getAsString();
            logger.info("api cui response-------"+cuiString);
            // Split the CUI string into an array and add to currentIterationCuiSet
            String[] cuiArray = cuiString.split(",");
            for (String cui : cuiArray) {
                currentIterationCuiSet.add(cui.trim());
            }

            logger.info("currentIterationCuiSet response---"+currentIterationCuiSet);
            logger.info("currentCuiSet response---"+currentCuiSet);
            // Determine new CUIs and inactive CUIs
            Set<String> newCuis = new HashSet<>();
            Set<String> inactiveCuis = new HashSet<>(); 
           
           
            for (String cui : currentIterationCuiSet) {
                if (!currentCuiSet.contains(cui)) {                   	
                	  newCuis.add(cui);                     
                }

            }
            
            for (String cui : currentCuiSet) {
                if (!currentIterationCuiSet.contains(cui)) {                   	
                	inactiveCuis.add(cui);                      
                }

            }

            // Update currentCuiSet and inactiveCuiSet
            currentCuiSet.addAll(newCuis);
         

            // Create JSON arrays for new and inactive CUIs
            JsonArray newCuiArray = new JsonArray();
            JsonArray inactiveCuiArray = new JsonArray();

            for (String newCui : newCuis) {
                newCuiArray.add(newCui);
            }

            for (String inactiveCui : inactiveCuis) {
                inactiveCuiArray.add(inactiveCui);
            }

            logger.info("final newCuiArray"+newCuiArray +"inactiveCuiArray-->"+ inactiveCuiArray);
            // Send data to cloud service
            JsonObject apiResponse = RestCloudActionService.sendCuiData(newCuiArray, inactiveCuiArray,authToken);

            // Store the updated CuiData
            cuiData = new JsonArray();
            for (String cui : currentCuiSet) {
            	cuiData.add(cui);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Error--"+e);
            
        }
    }
    private void processPlanB() {
    	 JsonArray mesajeArr = null;
    	 try {    	      
    	        String url = storedId == null ? 
    	            "https://webserviced.anaf.ro/SPVWS2/rest/listaMesaje?zile=1" :
    	            "https://webserviced.anaf.ro/SPVWS2/rest/listaMesaje?zile=50";
    	        logger.info("url------->" + url);
    	        JsonObject response = RestPlanService.executePlan(url);     
    	        if(response!=null)
    	        {
    	          mesajeArr = response.getAsJsonArray("mesaje");
    	        }
    	        
    	        logger.info("mesajeArr------->" + mesajeArr);
    	        if (mesajeArr != null && mesajeArr.size() > 0) {
    	          
    	         
    	            if (storedId != null) {
    	            	 logger.info("second time call filterMessagesAfterStoredId");
    	                mesajeArr = filterMessagesAfterStoredId(mesajeArr, storedId);
    	            }
                    
    	            if (mesajeArr == null || mesajeArr.size() == 0) {
    	                logger.info("No messages found after filtering. Aborting further processing.");
    	                return; // Exit the method if no messages are left after filtering
    	            }
    	           
    	            logger.info("after filter mesajeArr response-------" + mesajeArr);

    	            // Split messages into smaller packages if necessary
    	            List<JsonArray> messagePackages = splitIntoPackages(mesajeArr,200); // Adjust package size as needed

    	            // Send each package to the cloud
    	            for (JsonArray packageArr : messagePackages) {
    	            	 logger.info("RestCloudActionService req array-------" + packageArr);
    	                JsonObject apiResponse = RestCloudActionService.sendMesajeData(packageArr, authToken);
    	                logger.info("RestCloudActionService response-------" + apiResponse);
    	            }

    	            // Extract and process IDs
    	            Set<String> extractedIds = extractIds(mesajeArr);
    	            String highestId = processExtractedIds(extractedIds);
    	            logger.info("highestId------------------" + highestId);
    	            storedId = highestId ;

    	        } else {
    	            logger.warn("Invalid response format");
    	        }
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        logger.warn("Error--" + e);
    	    }

      
    }
    
    private JsonArray filterMessagesAfterStoredId(JsonArray mesajeArr, String storedId) {
        JsonArray filteredArr = new JsonArray();
        for (JsonElement element : mesajeArr) {
            JsonObject message = element.getAsJsonObject();
            String messageId = message.get("id").getAsString();

            if (Long.parseLong(messageId) > Long.parseLong(storedId)) {
                filteredArr.add(message);
            }
        }
        return filteredArr;
    }


    private List<JsonArray> splitIntoPackages(JsonArray mesajeArr, int packageSize) {
        List<JsonArray> packages = new ArrayList<>();
        JsonArray currentPackage = new JsonArray();
        for (JsonElement element : mesajeArr) {
            currentPackage.add(element);
            if (currentPackage.size() == packageSize) {
                packages.add(currentPackage);
                currentPackage = new JsonArray();
            }
        }
        if (currentPackage.size() > 0) {
            packages.add(currentPackage);
        }
        return packages;
    }

    private Set<String> extractIds(JsonArray mesajeArr) {
        Set<String> ids = new HashSet<>();
        for (JsonElement element : mesajeArr) {
            JsonObject message = element.getAsJsonObject();
            ids.add(message.get("id").getAsString());
        }
        return ids;
    }

    private String processExtractedIds(Set<String> extractedIds) {
        String highestId = "";
        try {
            long maxId = -1; 

            for (String id : extractedIds) {
                File file = RestPlanService.getFile("https://webserviced.anaf.ro/SPVWS2/rest/descarcare?id="+id,"C:\\Documents\\contai\\documente SPV1");  
               
                long numericId = Long.parseLong(id.trim());

                if (numericId > maxId) {
                    maxId = numericId;
                    highestId = id; 
                }
            }

        } catch (NumberFormatException e) {
            logger.warn("ID format issue: " + e.getMessage());
        }

        return highestId; // Return the highest ID as a string
    }

    private void processPlanC() {
        try {
            Set<String> currentCuiSet = new HashSet<>();

           
           
            if (cuiData != null) {
                for (JsonElement element : cuiData) {
                    if (element.isJsonPrimitive()) {
                        String cui = element.getAsString();
                        currentCuiSet.add(cui.trim());
                    }
                }
            }

 
            
            for (String cui : currentCuiSet) {
            	 logger.info("plan c cui req---"+cui);
                JsonObject response = RestPlanService.executePlan("https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=VECTOR%20FISCAL&cui="+cui.trim());               
                logger.info("plan c cui res---"+response);
                
                
                Thread.sleep(1100);
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Error--"+e);
            
        }
    }
    
    
    private void processPlanD() {
        try {
            Set<String> currentCuiSet = new HashSet<>();

           
           
            if (cuiData != null) {
                for (JsonElement element : cuiData) {
                    if (element.isJsonPrimitive()) {
                        String cui = element.getAsString();
                        currentCuiSet.add(cui.trim());
                    }
                }
            }

 
            
            for (String cui : currentCuiSet) {
            	 logger.info("plan d cui req---"+cui);
                JsonObject response = RestPlanService.executePlan("https://webserviced.anaf.ro/SPVWS2/rest/cerere?tip=Fisa%20Rol&cui="+cui.trim());               
                logger.info("plan d cui res---"+response);
              
                Thread.sleep(1100);
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Error--"+e);
            
        }
    }
    
    public void cleanup() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

}