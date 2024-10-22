package contai.service;

import com.google.gson.JsonArray;
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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import org.apache.log4j.Logger;

public class RestSecurityService {

    private static final Logger LOGGER = Logger.getLogger(RestSecurityService.class);
    private static final String BASE_URL = "https://spv-dev.contai.ro/api";
    private static final String SECURITY_TOKENS_URL = BASE_URL + "/security-tokens";
    private static final String POLLING_URL = BASE_URL + "/app-events/unacknowledged";
    private static final String POLLING_ACKNOWLEDGE_URL = BASE_URL + "/app-events/acknowledge";

    public static JsonObject getAddAllCertificates(String authToken) throws IOException {
        JsonArray certificatesArray = new JsonArray();
        JsonObject requestBody = new JsonObject();

        try {
            KeyStore ks = KeyStore.getInstance("Windows-MY");
            ks.load(null, null);

            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
                if (cert != null) {
                    JsonObject certObject = new JsonObject();
                    certObject.addProperty("name", alias);
                    certObject.addProperty("issuer", cert.getIssuerDN().getName());
                    certObject.addProperty("serial_number", cert.getSerialNumber().toString(16));
                    certObject.addProperty("expires_at", cert.getNotAfter().toInstant().toString());

                    certificatesArray.add(certObject);
                }
            }
            LOGGER.info(certificatesArray + "====certificatesArray==================");
            // Check if the certificatesArray is not empty before adding it to requestBody
            if (certificatesArray.size() > 0) {
                requestBody.add("certificates", certificatesArray);
                LOGGER.info(requestBody + "====requestBody==================");

                // Set up the POST request
                URL url = new URL(SECURITY_TOKENS_URL); // Replace SECURITY_TOKENS_URL with the actual URL
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + authToken); // Add the auth token
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Send the JSON body
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Check the response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Handle success (parse the response if needed)
                    LOGGER.info("POST request sent successfully. Response code: " + responseCode);
                } else {
                    LOGGER.error("Failed to send POST request. Response code: " + responseCode);
                }
            } else {
                LOGGER.error("No certificates found to send in POST request.");
            }

        } catch (Exception e) {
            LOGGER.error("Error processing certificates: " + e.getMessage(), e);
        }

        return requestBody;
    }


    public static JsonObject listAllEvents(String authToken) throws IOException {
    	
        return sendGetRequest(POLLING_URL, authToken);
    }

    public static JsonObject acknowledgeEvent(String authToken, String eventId) throws IOException {
        URL url = new URL(POLLING_ACKNOWLEDGE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Cookie", "token=" + authToken);
        connection.setDoOutput(true);

        // Create JSON body with the eventId
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("eventId", eventId);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
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
                LOGGER.info("Response acknowledge polling event: " + response.toString());
                return new JsonParser().parse(response.toString()).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                LOGGER.error("Failed to parse JSON response", e);
                throw new IOException("Failed to parse JSON response", e);
            }
        } finally {
            connection.disconnect();
        }
    }

    public static JsonObject getAllPinCertificates(String authToken) throws IOException {
        return sendGetRequest(SECURITY_TOKENS_URL, authToken);
    }

    private static JsonObject sendGetRequest(String urlString, String authToken) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cookie", "token=" + authToken);
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
                    LOGGER.info("Response: " + response.toString());
                    return new JsonParser().parse(response.toString()).getAsJsonObject();
                } catch (JsonSyntaxException e) {
                    LOGGER.error("Failed to parse JSON response", e);
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
