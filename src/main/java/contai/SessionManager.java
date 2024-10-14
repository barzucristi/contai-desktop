package contai;

import java.util.prefs.Preferences;
import javax.swing.*;
import org.apache.log4j.Logger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Base64;

import contai.forms.LoginForm;
import contai.forms.FolderPathPage;
import contai.utilities.PlanExecutor;
import contai.service.RestLoginService;

public class SessionManager {
    private static final Logger LOGGER = Logger.getLogger(SessionManager.class);
    private static final String AUTH_TOKEN_KEY = "authToken";
    private static final String PAGE_KEY = "page";
    private static final String HOT_FOLDER_PATH_KEY = "hotFolderPath";
    private static final String SIGNED_DOCS_FOLDER_PATH_KEY = "signedDocsFolderPath";
    private static final String SPV_DOCS_FOLDER_PATH_KEY = "spvDocsFolderPath";
    

    
    private JFrame mainFrame;
    private Preferences prefs;
    private PlanExecutor planExecutor;
    private JPanel loginPanel;

    public SessionManager(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.prefs = Preferences.userRoot().node(ContAiApp.class.getName());
    }

    public void initializeSession() {
        String authToken = prefs.get(AUTH_TOKEN_KEY, null);
        LOGGER.info("Auth token status: " + (authToken != null ? "Found" : "Not found"));

        initializeFolderPaths();
        
        if (authToken != null && isTokenValid(authToken)) {
            LOGGER.info("Valid auth token found, proceeding to folder path setup.");
            showFolderPathPage(mainFrame);
            callApiAndUpdateUI(authToken);
        } else {
            LOGGER.info("No valid auth token, showing login form.");
            prefs.remove(AUTH_TOKEN_KEY);
            showLoginForm();
        }
    }

    private boolean isTokenValid(String authToken) {
        try {
            String[] parts = authToken.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonObject payload = new JsonParser().parse(payloadJson).getAsJsonObject();
            long expTime = payload.get("exp").getAsLong() * 1000;
            return System.currentTimeMillis() < expTime;
        } catch (Exception e) {
            LOGGER.error("Error validating token", e);
            return false;
        }
    }


    public void login(String username, String password) {
        try {
            JsonObject response = RestLoginService.login(username, password);

            if (response != null && response.has("status")) {
                int status = response.get("status").getAsInt();

                if (status == 200) {
                    handleSuccessfulLogin(response);
                } else {
                    handleFailedLogin(response);
                }
            } else {
                LOGGER.warn("Invalid response format from server");
                JOptionPane.showMessageDialog(loginPanel, "The server response was invalid. Please try again later or contact support.", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred during login", e);
            JOptionPane.showMessageDialog(loginPanel, "An unexpected error occurred during login. Please check your internet connection and try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSuccessfulLogin(JsonObject response) {
        JsonObject data = response.getAsJsonObject("data");
        SwingUtilities.invokeLater(() -> {
            if (data != null && data.has("token") && data.has("user") && data.has("cui")) {
                JsonObject userData = data.getAsJsonObject("user");
                JsonObject cui = data.getAsJsonObject("cui");
                String authToken = data.get("token").getAsString();
                LOGGER.info("Login successful. Auth token received.");
                prefs.put(AUTH_TOKEN_KEY, authToken);
                
            	showFolderPathPage(mainFrame);
                setupPlanExecutor(authToken, userData, cui);
            } else {
                LOGGER.warn("Auth token not found in response data");
                JOptionPane.showMessageDialog(loginPanel, "Login successful, but authentication token is missing. Please try again or contact support.", "Login Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
    }


    private void handleFailedLogin(JsonObject response) {
        String message = response.has("message") ? response.get("message").getAsString() : "An unknown error occurred during login. Please try again.";
        LOGGER.warn("Login failed. Message: " + message);
        JOptionPane.showMessageDialog(loginPanel, message, "Login Error", JOptionPane.ERROR_MESSAGE);
    }

    public void logout() {
        try {
            prefs.remove(AUTH_TOKEN_KEY);
            prefs.remove(PAGE_KEY);
            if (planExecutor != null) {
                planExecutor.cleanup();
                planExecutor = null;
            }
            showLoginForm();
        } catch (Exception e) {
            LOGGER.error("Error during logout", e);
        }
    }

    private void showLoginForm() {
    	LOGGER.info("Main frame: " + mainFrame); 
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm(mainFrame,this);
            mainFrame.setContentPane(loginForm.getPanel());
            mainFrame.revalidate();
            mainFrame.repaint();
        });
    }
    public void showFolderPathPage(JFrame frame) {
        SwingUtilities.invokeLater(() -> {
            FolderPathPage folderPathPage = new FolderPathPage(frame,this);
            JPanel folderPathPanel = folderPathPage.getPanel();
            frame.setContentPane(folderPathPanel);
            frame.revalidate();
            frame.repaint();
        });
    }


    private void setupPlanExecutor(String authToken, JsonObject userData, JsonObject cui) {
        planExecutor = new PlanExecutor(authToken, userData, cui);
        planExecutor.setupPlans();
    }

    private void callApiAndUpdateUI(String authToken) {
        try {
            JsonObject response = RestLoginService.desktop(authToken);

            if (response != null && response.has("status")) {
                int status = response.get("status").getAsInt();

                if (status == 200) {
                    JsonObject data = response.getAsJsonObject("data");

                    if (data != null) {
                        JsonObject userData = data.getAsJsonObject("user");
                        JsonObject cui = data.getAsJsonObject("cui");

                        if (cui != null && userData != null) {
                            setupPlanExecutor(authToken, userData, cui);
                        } else {
                            LOGGER.warn("User data or CUI information is missing in the response");
                        }

                    } else {
                        LOGGER.warn("User data not found in response");
                    }
                } else {
                    String message = response.has("message") ? response.get("message").getAsString() : "An unknown error occurred. Please try again.";
                    LOGGER.warn("API call failed with status: " + status + ". Message: " + message);
                }
            } else {
                LOGGER.warn("Invalid response format from server");
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred during API call", e);
        }
    }

    public PlanExecutor getPlanExecutor() {
        return planExecutor;
    }
    
    
    private void initializeFolderPaths() {
        // Get folder paths from Preferences (if available)
        String hotFolderPath = prefs.get(HOT_FOLDER_PATH_KEY, null);
        String signedDocsFolderPath = prefs.get(SIGNED_DOCS_FOLDER_PATH_KEY, null);
        String spvDocsFolderPath = prefs.get(SPV_DOCS_FOLDER_PATH_KEY, null);
        
        if (hotFolderPath == null || signedDocsFolderPath == null || spvDocsFolderPath == null) {
            // First time setup: store default paths
            LOGGER.info("Setting default folder paths...");
            hotFolderPath = "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\Declaratii nesemnate";
            signedDocsFolderPath = "C:\\Documents\\contai\\declaratii semnate";
            spvDocsFolderPath = "C:\\Documents\\contai\\documente SPV";
            
            prefs.put(HOT_FOLDER_PATH_KEY, hotFolderPath);
            prefs.put(SIGNED_DOCS_FOLDER_PATH_KEY, signedDocsFolderPath);
            prefs.put(SPV_DOCS_FOLDER_PATH_KEY, spvDocsFolderPath);
        } else {
            // Retrieve paths from Preferences
            LOGGER.info("Folder paths retrieved from cookies (Preferences):");
            LOGGER.info("Hot folder path: " + hotFolderPath);
            LOGGER.info("Signed docs folder path: " + signedDocsFolderPath);
            LOGGER.info("SPV docs folder path: " + spvDocsFolderPath);
        }
    }
}
