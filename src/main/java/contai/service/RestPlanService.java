package contai.service;

import java.io.File;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

import sqw.apelspv.ApelSPV;
import sqw.apelspv.ApelSPVFile;

public class RestPlanService {

    private static final Logger LOGGER = Logger.getLogger(RestPlanService.class);
  

    public static JsonObject executePlan(String apiUrl) {
        ApelSPV apelSPV = new ApelSPV(); // Create an instance of ApelSPV
        
        // Call the API and get the response
        JsonObject response = apelSPV.makeApiCall(apiUrl);
        
        // Log the response
        if (response != null) {
            LOGGER.info("API Response:==== " + response);
        } else {
            LOGGER.error("Failed to retrieve response from API");
            return null; // Return null if no response
        }

      
        return response; // Return the list of CUI values
    }
    
    
    public static File getFile(String apiUrl, String destinationPath) {
        ApelSPVFile apelSPV = new ApelSPVFile(); // Create an instance of ApelSPV

        // Call the API and get the response
        File pdfFile = apelSPV.makeApiCall(apiUrl, destinationPath);

        // Log the response
        if (pdfFile != null) {
            LOGGER.info("PDF file saved at: " + pdfFile.getAbsolutePath());
        } else {
            LOGGER.error("Failed to retrieve PDF from API");
            return null; // Return null if no response
        }

        return pdfFile; // Return the PDF file
    }

}
