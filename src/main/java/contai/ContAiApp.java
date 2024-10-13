package contai;

import java.util.prefs.Preferences;
import javax.swing.*;
import contai.forms.FolderPathPage;
import contai.forms.LoginForm;
import org.apache.log4j.Logger;
import java.util.Base64;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ContAiApp {

    // Create a logger instance
    private static final Logger LOGGER = Logger.getLogger(ContAiApp.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ContAi");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            LOGGER.info("Application started.");

            // Correct usage of Preferences in a static context
            Preferences prefs = Preferences.userRoot().node(ContAiApp.class.getName());
            prefs.remove("page");
            String authToken = prefs.get("authToken", null);
            LOGGER.info("authToken------------>" + authToken);

            // Check if authToken is present
            if (authToken != null) {
            	prefs.put("page","2");
                // Decode the JWT
                String[] parts = authToken.split("\\.");
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                LOGGER.info("Payload: " + payloadJson);

                // Parse the payload for expiration time using JsonParser
                JsonObject payload = new  JsonParser().parse(payloadJson).getAsJsonObject();
                long expTime = payload.get("exp").getAsLong() * 1000; // Convert to milliseconds
                LOGGER.info("Expiration Time: " + expTime);

                // Check if the token is still valid
                if (System.currentTimeMillis() < expTime) {
                    LOGGER.info("Auth token found and valid, skipping login.");
                    // If authToken exists and is valid, skip login and go directly to the folder path setup
                    FolderPathPage folderPathPage = new FolderPathPage(frame);
                    frame.setContentPane(folderPathPage.getPanel());
                    prefs.put("page","2");
                    
                } else {
                    LOGGER.info("Auth token expired, showing login form.");
                    // Clear the expired token
                    prefs.remove("authToken");
                    prefs.put("page","1");
                  

                    // Show the login form
                    LoginForm loginForm = new LoginForm(frame);
                    frame.setContentPane(loginForm.getPanel());
                }
            } else {
                LOGGER.info("No auth token found, showing login form.");
                // If no authToken, show the login form
                LoginForm loginForm = new LoginForm(frame);
                frame.setContentPane(loginForm.getPanel());
                prefs.put("page","1");
            }

            frame.setVisible(true);
        });
    }
}
