package contai.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RestCloudActionService {

    private static final Logger logger = Logger.getLogger(RestCloudActionService.class);
    private static final String CLOUD_ACTION_URL = "https://spv-dev.contai.ro/api/cloud-actions/1";

    public static JsonObject sendCuiData(JsonArray newCuiArray, JsonArray inactiveCuiArray,String authToken) throws IOException {
        HttpURLConnection connection = null;
        logger.error("active Array:--: " + newCuiArray);
        logger.error("inactive Array:--: " + inactiveCuiArray);
        try {
            URL url = new URL(CLOUD_ACTION_URL);
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
}
