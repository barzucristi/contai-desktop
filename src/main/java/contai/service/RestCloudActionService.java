package contai.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RestCloudActionService {

    private static final Logger logger = Logger.getLogger(RestCloudActionService.class);
    private static final String BASE_URL = "https://spv-dev.contai.ro/api";
    private static final String CLOUD_ACTION_URL_1 = BASE_URL+"/cloud-actions/1";
    private static final String CLOUD_ACTION_URL_2 = BASE_URL+"/cloud-actions/2";

    public static JsonObject sendCuiData(JsonArray newCuiArray, JsonArray inactiveCuiArray,String authToken) throws IOException {
        HttpURLConnection connection = null;
        logger.error("1)active Array:--: " + newCuiArray);
        logger.error("2)inactive Array:--: " + inactiveCuiArray);
        try {
            URL url = new URL(CLOUD_ACTION_URL_1);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cookie", "token="+authToken);
            connection.setDoOutput(true);

            // Construct the JSON request body
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.add("new_cui", newCuiArray);
            jsonRequest.add("inactive_cui", inactiveCuiArray);

            // Send request body
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonRequest.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // Read response
            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode == HttpURLConnection.HTTP_OK
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                logger.info(response.toString());
                
                // Check for session expiration
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    logger.error("Session expired: " + response.toString());
                    // Handle session expiration (e.g., re-authenticate or notify user)
                    // You may throw a custom exception or return null here based on your design
                    return null; // or throw a custom exception
                }
                
                logger.error("response: " + response.toString());
            	return new JsonParser().parse(response.toString()).getAsJsonObject();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    
    public static JsonObject sendMesajeData(JsonArray mesajeArr, String authToken) throws IOException {
        HttpURLConnection connection = null;
        logger.info("Sending messages data: " + mesajeArr);

        try {
            URL url = new URL(CLOUD_ACTION_URL_2);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cookie", "token=" + authToken);
            connection.setDoOutput(true);

            // Construct the JSON request body
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.add("messages", mesajeArr);

            // Send request body
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonRequest.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // Read response
            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode == HttpURLConnection.HTTP_OK
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                logger.info("Response received: " + response.toString());

                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    logger.error("Session expired: " + response.toString());
                    return null;
                }

                return new JsonParser().parse(response.toString()).getAsJsonObject();
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    public static boolean uploadDocumentFile(File file, String documentId, String authToken) throws IOException {
        HttpURLConnection connection = null;
    	String uploadUrl =  BASE_URL+"/document-messages/" + documentId + "/upload";
     
        String boundary = "Boundary-" + System.currentTimeMillis();
        try {
        URL url = new URL(uploadUrl);
        connection =  (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Cookie", "token=" + authToken);

        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append("\r\n");
            writer.append("Content-Type: " + Files.probeContentType(file.toPath())).append("\r\n");
            writer.append("\r\n");
            writer.flush();

            Files.copy(file.toPath(), outputStream);
            outputStream.flush();

            writer.append("\r\n");
            writer.append("--" + boundary + "--").append("\r\n");
        }
        // Get the response
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            logger.info("File uploaded successfully: " + file.getName());
            return true;
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            logger.error("Session expired or unauthorized. Response code: " + responseCode);
            return false;
        } else {
            logger.error("Failed to upload file. Response code: " + responseCode);
            return false;
        }
    } finally {
        if (connection != null) {
            connection.disconnect();
        }
    }
}

}
