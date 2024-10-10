package contai.forms;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;


import com.google.gson.*;

import contai.ContAiApp;
import contai.service.RestCloudActionService;
import contai.service.RestLoginService;
import contai.service.RestPlanService;

public class LoginForm {
	private static final Logger logger = Logger.getLogger(LoginForm.class);

    private JPanel panel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JFrame parentFrame;
    private JCheckBox rememberMeCheckbox;
    private Timer timer;
    private JsonObject userData;
    private String authToken;
    private JsonArray CuiData = new JsonArray();
    private  String storedId = null;
  
    public LoginForm(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.timer = new Timer();
    }

    public JPanel getPanel() {
        panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JPanel loginPanel = createLoginPanel();
        JPanel rightPanel = createRightPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, loginPanel, rightPanel);
        splitPane.setDividerLocation(600);
        splitPane.setEnabled(false);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        // Logo
        JLabel logo = new JLabel();
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/images/ai.png"));
        logo.setIcon(new ImageIcon(logoIcon.getImage().getScaledInstance(75, 55, Image.SCALE_SMOOTH)));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(logo, gbc);

        // Login label
        JLabel loginLabel = new JLabel("Log in to your account");
        loginLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        loginLabel.setForeground(Color.decode("#333333"));
        gbc.gridy = 1;
        loginPanel.add(loginLabel, gbc);

        // Welcome label
        JLabel welcomeLabel = new JLabel("Welcome back! Please enter your details.");
        welcomeLabel.setFont(new Font("Poppins", Font.PLAIN, 16));
        welcomeLabel.setForeground(Color.decode("#333333"));
        gbc.gridy = 2;
        loginPanel.add(welcomeLabel, gbc);

        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Poppins", Font.BOLD, 14));
        usernameLabel.setForeground(Color.decode("#333333"));
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(usernameLabel, gbc);

        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(360, 44));
        usernameField.setFont(new Font("Poppins", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#CCCCCC"), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        gbc.gridy = 4;
        loginPanel.add(usernameField, gbc);

        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Poppins", Font.BOLD, 14));
        passwordLabel.setForeground(Color.decode("#333333"));
        gbc.gridy = 5;
        loginPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(360, 44));
        passwordField.setFont(new Font("Poppins", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#CCCCCC"), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        gbc.gridy = 6;
        loginPanel.add(passwordField, gbc);

        // Remember me and Forgot password
        JPanel lowerPanel = new JPanel(new GridBagLayout());
        lowerPanel.setOpaque(false);

        rememberMeCheckbox = new JCheckBox("Remember for 30 days");
        rememberMeCheckbox.setBackground(Color.WHITE);
        GridBagConstraints lowerGbc = new GridBagConstraints();
        lowerGbc.insets = new Insets(0, 0, 0, 0);
        lowerGbc.anchor = GridBagConstraints.WEST;
        lowerGbc.weightx = 1.0;
        lowerGbc.gridx = 0;
        lowerGbc.gridy = 0;
        lowerPanel.add(rememberMeCheckbox, lowerGbc);

        JLabel forgotPassword = new JLabel("<html><u>Forgot password?</u></html>");
        forgotPassword.setFont(new Font("Poppins", Font.BOLD, 14));
        forgotPassword.setForeground(Color.decode("#2979FF"));
        GridBagConstraints forgotPasswordGbc = new GridBagConstraints();
        forgotPasswordGbc.insets = new Insets(0, 80, 0, 0);
        forgotPasswordGbc.anchor = GridBagConstraints.EAST;
        forgotPasswordGbc.gridx = 1;
        forgotPasswordGbc.gridy = 0;
        lowerPanel.add(forgotPassword, forgotPasswordGbc);

        forgotPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(loginPanel, "Password recovery feature not implemented yet.");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                forgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                forgotPassword.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        gbc.gridy = 7;
        gbc.gridwidth = 2;
        loginPanel.add(lowerPanel, gbc);

        // Login button
        JButton loginButton = new JButton("Sign In");
        loginButton.setBackground(Color.decode("#2979FF"));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Poppins", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(360, 44));
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateAndLogin();
            }
        });
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        loginPanel.add(loginButton, gbc);

        // Google Sign In button
        JButton googleSignInButton = new JButton("Sign in with Google");
        googleSignInButton.setBackground(Color.WHITE);
        googleSignInButton.setForeground(Color.decode("#DB4437"));
        googleSignInButton.setFont(new Font("Poppins", Font.BOLD, 14));
        googleSignInButton.setPreferredSize(new Dimension(360, 44));

        ImageIcon googleIcon = new ImageIcon(getClass().getResource("/images/google.png"));
        googleSignInButton.setIcon(new ImageIcon(googleIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        googleSignInButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        googleSignInButton.setIconTextGap(10);

        gbc.gridy = 9;
        loginPanel.add(googleSignInButton, gbc);

        // Footer
        JLabel footerLabel = new JLabel("� Example 2024");
        footerLabel.setFont(new Font("Poppins", Font.PLAIN, 14));
        footerLabel.setForeground(Color.decode("#333333"));
        GridBagConstraints footerGbc = new GridBagConstraints();
        footerGbc.insets = new Insets(70, 10, -40, 10);
        footerGbc.anchor = GridBagConstraints.SOUTHWEST;
        footerGbc.gridx = 0;
        footerGbc.gridy = 10;
        gbc.gridwidth = 2;
        loginPanel.add(footerLabel, footerGbc);

        return loginPanel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0x1B0B4F), getWidth(), getHeight(), new Color(0x57B2D9));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        rightPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.insets = new Insets(10, 0, 10, 0);

        JLabel mockupLabel = new JLabel();
        ImageIcon mockupIcon = new ImageIcon(getClass().getResource("/images/Mockup.png"));
        mockupLabel.setIcon(new ImageIcon(mockupIcon.getImage().getScaledInstance(432, 328, Image.SCALE_SMOOTH)));

        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.anchor = GridBagConstraints.CENTER;
        rightPanel.add(mockupLabel, gbcRight);

        return rightPanel;
    }

    private void validateAndLogin() {
        String username = usernameField != null ? usernameField.getText() : null;
        String password = passwordField != null ? new String(passwordField.getPassword()) : null;

        if (username == null || password == null) {
            JOptionPane.showMessageDialog(panel, "Username and Password fields must be initialized.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (username.isEmpty() && password.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Username and Password are required.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Username is required.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Password is required.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            JsonObject response = RestLoginService.login(username, password);

            if (response != null && response.has("status")) {
                int status = response.get("status").getAsInt();

                if (status == 200) {
                
                    JsonObject Data = response.getAsJsonObject("data");
                    
                    
                    
                    if (Data != null) {
                    	  if (Data.has("token")) {
                              authToken = Data.get("token").getAsString(); // Extract the token
                              logger.info("authToken: " + authToken); // Log the token

                              Preferences prefs = Preferences.userRoot().node(ContAiApp.class.getName()); // Ensure node consistency
                              prefs.put("authToken", authToken); // Store the token
                              
                          } else {
                              logger.warn("Token not found in response data.");
                          }
                       
                        userData = Data.getAsJsonObject("user");
                        JsonObject cui = Data.getAsJsonObject("cui");
                        
                       
                        if (cui != null) {
                            JsonArray activeArray = cui.has("active") && cui.get("active").isJsonArray()
                                ? cui.getAsJsonArray("active")
                                : new JsonArray();
                          
                            for (JsonElement element : activeArray) {
                            	  if (element.isJsonPrimitive()) {
                            		  CuiData.add(element); 
                            	  }
                            }    
                                                 
                            setupPlans();
                            FolderPathPage folderPathSetupPage = new FolderPathPage(parentFrame);
                            JPanel folderPathSetupPanel = folderPathSetupPage.getPanel();
                            parentFrame.setContentPane(folderPathSetupPanel);
                            parentFrame.revalidate();
                            parentFrame.repaint();
                        }
                    } else {
                        String message = "User data not found in response.";
                        JOptionPane.showMessageDialog(panel, message, "Login Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    String message = response.has("message") ? response.get("message").getAsString() : "An error occurred during login.";
                    JOptionPane.showMessageDialog(panel, message, "Login Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                String message = "Response from server is null or invalid.";
                JOptionPane.showMessageDialog(panel, message, "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            String message = "An error occurred during login.";
            JOptionPane.showMessageDialog(panel, message, "Login Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


  

        private void setupPlans() {
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
         logger.info("Triggering plan: " + plan.get("name").getAsString() + " at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
         logger.info("Frequency Type: " + type);
         if (value != null) {
         	logger.info("Frequency Value: " + value);
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
//                   	logger.info("Executing actions for Plan A with frequency type: " + frequencyType + " and value: " + frequencyValue);
                	processPlanA();
                    break;
                case "Plan B":
                	processPlanB();
                    // Trigger specific actions for Plan B
//                	logger.info("Executing actions for Plan B with frequency type: " + frequencyType + " and value: " + frequencyValue);
                    break;
                case "Plan C":
                	processPlanC();
                    // Trigger specific actions for Plan C
//                	logger.info("Executing actions for Plan C with frequency type: " + frequencyType + " and value: " + frequencyValue);
                    break;
                case "Plan D":
                	processPlanD(); 
                    // Trigger specific actions for Plan D
//                	logger.info("Executing actions for Plan D with frequency type: " + frequencyType + " and value: " + frequencyValue);
                    break;
                // Add cases for other plans as needed
                default:
//                	logger.info("Unknown plan: " + planName);
                    break;
            }
        }

        private void processPlanA() {
            try {
                Set<String> currentIterationCuiSet = new HashSet<>();
                Set<String> currentCuiSet = new HashSet<>();

                // First, use existing CuiData
                logger.info("CuiData response---"+CuiData);
               
                if (CuiData != null) {
                    for (JsonElement element : CuiData) {
                        if (element.isJsonPrimitive()) {
                            String cui = element.getAsString();
                            currentCuiSet.add(cui.trim());
                        }
                    }
                }

                
          
                // Then, check from API
                JsonObject response = RestPlanService.executePlan("https://webserviced.anaf.ro/SPVWS2/rest/listaMesaje?zile=500");               
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
                CuiData = new JsonArray();
                for (String cui : currentCuiSet) {
                    CuiData.add(cui);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(panel, e.getMessage(), "Response", JOptionPane.ERROR_MESSAGE);
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

               
               
                if (CuiData != null) {
                    for (JsonElement element : CuiData) {
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
                JOptionPane.showMessageDialog(panel, e.getMessage(), "Response", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                logger.warn("Error--"+e);
                
            }
        }
        
        
        private void processPlanD() {
            try {
                Set<String> currentCuiSet = new HashSet<>();

               
               
                if (CuiData != null) {
                    for (JsonElement element : CuiData) {
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
                JOptionPane.showMessageDialog(panel, e.getMessage(), "Response", JOptionPane.ERROR_MESSAGE);
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