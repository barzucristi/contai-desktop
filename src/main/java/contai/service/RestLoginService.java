package contai.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;

public class RestLoginService {

    private static final String LOGIN_URL = "https://spv-dev.contai.ro/api/auth/login";
    private static final String DESKTOP_URL = "https://spv-dev.contai.ro/api/desktop/me";
    private static final Logger LOGGER = Logger.getLogger(RestLoginService.class);
    
    public static JsonObject login(String email, String password) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(LOGIN_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            // Send request body
            String platform = "desktop"; // or "web"
            String jsonInputString = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"platform\":\"%s\"}", email, password, platform);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // Read response
            InputStream stream = connection.getResponseCode() == HttpURLConnection.HTTP_OK
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                try {
                	LOGGER.info("Response login--"+response.toString());
                	return new JsonParser().parse(response.toString()).getAsJsonObject();
                } catch (JsonSyntaxException e) {
                	LOGGER.info("Response login--"+e);
                    throw new IOException("Failed to parse JSON response", e);
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    
    public static JsonObject desktop(String authToken) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(DESKTOP_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cookie", "token="+authToken);
            connection.setDoOutput(true);

         
            // Read response
            InputStream stream = connection.getResponseCode() == HttpURLConnection.HTTP_OK
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                try {
                	LOGGER.info("Response login--"+response.toString());
                	return new JsonParser().parse(response.toString()).getAsJsonObject();
                } catch (JsonSyntaxException e) {
                	LOGGER.info("Response login--"+e);
                    throw new IOException("Failed to parse JSON response", e);
                }
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

