package contai.forms;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import general.Integrator;

public class FolderPathSetupPage {
    private JPanel panel;
    private JFrame parentFrame;
    private static final String API_URL = "https://decl.anaf.mfinante.gov.ro/";
    private static final String PDF_DIRECTORY = System.getProperty("user.dir") + File.separator + "pdfs";
    private static final String PDF_PATH = PDF_DIRECTORY + File.separator + "output.pdf";
    private static final String PDF_PATH_SIGNED = PDF_DIRECTORY + File.separator + "signed_output.pdf";
    private static final String CHROME_DRIVER_PATH = System.getProperty("user.dir") + File.separator +  "chromedriver-win64"+ File.separator +  "chromedriver.exe";
   
    public FolderPathSetupPage(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        createPdfDirectory();  // Ensure the directory exists when the page is created
    }

    public JPanel getPanel() {
        panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        
        JButton pdfValidateSign = new JButton("Pdf Validate And Digital Sign");
        JButton pdfValidate = new JButton("Step-1 Pdf Validate");
        JButton digitalSignPdf = new JButton("Step-2 Digital Sign PDF");
        JButton viewPdf = new JButton("Validate View Pdf");
        JButton removePdf = new JButton("Validate Remove Pdf");
        JButton viewPdfSigned = new JButton("Signed View Pdf");
        JButton removePdfSigned = new JButton("Signed Remove Pdf");
    
        JButton automaticUplaod = new JButton("Automatic upload");

        pdfValidate.addActionListener(e -> validateAndCreatePdf());
        viewPdf.addActionListener(e -> handleViewPdf());
        removePdf.addActionListener(e -> handleRemovePdf());
        digitalSignPdf.addActionListener(e -> digitalSignPdf());
        pdfValidateSign.addActionListener(e -> pdfValidateSign());
        
        viewPdfSigned.addActionListener(e -> handleViewPdfSigned());
        removePdfSigned.addActionListener(e -> handleRemovePdfSigned());

        automaticUplaod.addActionListener(e -> handleAutomaticUpload());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding between components
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(pdfValidateSign, gbc);

        gbc.gridx = 1;
        panel.add(pdfValidate, gbc);

        gbc.gridx = 2;
        panel.add(digitalSignPdf, gbc);

        gbc.gridx = 3;
        panel.add(viewPdf, gbc);

        gbc.gridx = 4;
        panel.add(removePdf, gbc);
        

        gbc.gridx = 5;
        panel.add(viewPdfSigned, gbc);

        gbc.gridx = 6;
        panel.add(removePdfSigned, gbc);
        
        gbc.gridx = 7;
        panel.add(automaticUplaod, gbc);
   
        
        return panel;
    }
    
    
    
    
    
    private void handleAutomaticUpload() {
        if (!new File(PDF_PATH_SIGNED).exists()) {
            System.out.println("Error: Signed PDF file not found.");
            return;
        }
        
//        String configName = "SmartCard";
//        Provider provider = Security.getProvider("SunPKCS11");
//        provider = provider.configure(KEYSTORE_CONFIG);
//        Security.addProvider(provider);
//
//        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE, KEYSTORE_PROVIDER);
//        keyStore.load(null, PIN);

//        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(CERTIFICATE_ALIAS);
//        PrivateKey privateKey = (PrivateKey) keyStore.getKey(CERTIFICATE_ALIAS, PIN);



        // Set ChromeDriver path
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);

        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--search-engine=Google"); 
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-search-engine-choice-screen");
//        options.addArguments("--ignore-certificate-errors");
//        options.addArguments("--allow-insecure-localhost");
//        options.setAcceptInsecureCerts(true);
//        options.addArguments("--user-data-dir="+P12_CERT_PATH);
//        options.addArguments("--profile-directory=Default");
//        options.addArguments("--ssl-client-certificate-file=" + P12_CERT_PATH);
//        options.addArguments("--ssl-client-key-password=" + CERTIFICATE_PASSWORD);

//        options.addArguments("ignore-certificate-errors");
//        options.addArguments("--headless"); 
       
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        try {
            System.out.println("Navigating to the upload page...");
            driver.get(API_URL);       
//            // Set the login configuration
//           String pkcs11Config = "path/to/pkcs11.cfg"; // Replace with the actual path to your config file
//            
//            // Add the SunPKCS11 provider dynamically
//            Provider p = new SunPKCS11(pkcs11Config);
//            Security.addProvider(p);
//            
//            // Load the KeyStore using the PKCS#11 provider
//            KeyStore keyStore = KeyStore.getInstance("PKCS11", p);
//            char[] pin = "Account123".toCharArray(); // Replace with your actual PIN
//            keyStore.load(null, pin);
//            
//            // Set up TrustManagerFactory using the PKCS#11 KeyStore
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            tmf.init(keyStore);
//
//            // Set up SSLContext
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, tmf.getTrustManagers(), null);
//            SSLContext.setDefault(sslContext);

          
            System.out.println("Waiting for the 'Prezentare certificat' button...");
            WebElement preSubmitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".credentials_input_submit")));
            preSubmitButton.click();
          
            
      
            System.out.println("Waiting for any page changes...");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("linkdoc")));

          
            System.out.println("Waiting for the file input field...");
            WebElement fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("linkdoc")));

           
            System.out.println("Uploading file...");
            fileInput.sendKeys(PDF_PATH_SIGNED);

           
            System.out.println("Waiting for and clicking the final submit button...");
            WebElement uploadButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='submit' and @value='Trimite']")));
            uploadButton.click();

            String pageSource = driver.getPageSource();
   
            String indexNumber = extractIndexNumber(pageSource);

            if (indexNumber != null) {
                System.out.println("File uploaded successfully! Index Number: " + indexNumber);
                JOptionPane.showMessageDialog(null,indexNumber, "Index Number", JOptionPane.INFORMATION_MESSAGE);
                
            } else {
                System.out.println("File uploaded, but Index Number not found!");
                JOptionPane.showMessageDialog(null,"Not found index", "Index Number", JOptionPane.ERROR_MESSAGE);
                
            }

        } catch (Exception e) {
            System.err.println("Error: An error occurred while uploading the file: " + e.getMessage());
        	JOptionPane.showMessageDialog(null,e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            
            e.printStackTrace();
        } finally {
            // Close the browser
            driver.quit();
        }
    }



    private static String extractIndexNumber(String pageSource) {
      
        Pattern pattern = Pattern.compile("Indexul\\s+este\\s*<b[^>]*>\\s*(\\d+)\\s*</b>");
        Matcher matcher = pattern.matcher(pageSource);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private void createPdfDirectory() {
        File directory = new File(PDF_DIRECTORY);
        if (!directory.exists()) {
            boolean created = directory.mkdirs(); 
            if (created) {
                System.out.println("PDF directory created successfully.");
            } else {
                System.err.println("Failed to create PDF directory.");
            }
        }
    }
    private void handleViewPdfSigned() {
        File pdfFile = new File(PDF_PATH_SIGNED);

        if (pdfFile.exists()) {
            try {
                Desktop.getDesktop().open(pdfFile);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Error opening the PDF file.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(panel, "PDF file not found at the specified path: " + PDF_PATH_SIGNED,
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRemovePdfSigned() {
        File pdfFile = new File(PDF_PATH_SIGNED);

        if (pdfFile.exists()) {
            boolean deleted = pdfFile.delete();
            if (deleted) {
                JOptionPane.showMessageDialog(panel, "PDF file successfully deleted!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to delete the PDF file.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(panel, "PDF file not found at the specified path: " + PDF_PATH_SIGNED,
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleViewPdf() {
        File pdfFile = new File(PDF_PATH);

        if (pdfFile.exists()) {
            try {
                Desktop.getDesktop().open(pdfFile);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Error opening the PDF file.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(panel, "PDF file not found at the specified path: " + PDF_PATH,
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRemovePdf() {
        File pdfFile = new File(PDF_PATH);

        if (pdfFile.exists()) {
            boolean deleted = pdfFile.delete();
            if (deleted) {
                JOptionPane.showMessageDialog(panel, "PDF file successfully deleted!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to delete the PDF file.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(panel, "PDF file not found at the specified path: " + PDF_PATH,
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void validateAndCreatePdf() {
        System.out.println("Path--->" + PDF_PATH);
        Integrator integrator = new Integrator();

        String configPath = new File("config").getAbsolutePath();
        integrator.setConfigPath(configPath);

        integrator.setDeclType("D390");

        String xmlFile = "xml/D390.xml";
        String errFile = "error.err";
        int validationResult = integrator.parseDocument(xmlFile, errFile);
        System.out.println("XML Validation result--: " + validationResult);
        System.out.println(integrator.getFinalMessage());

        if (validationResult >= 0) {
            // Create PDF
            String pdfFile = PDF_PATH;
            int pdfCreationResult = integrator.pdfCreation(xmlFile, errFile, null, pdfFile);
            System.out.println("PDF creation result--: " + pdfCreationResult);
            System.out.println(integrator.getFinalMessage());

            if (pdfCreationResult == 0) {
                JOptionPane.showMessageDialog(null, "PDF Validate successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void digitalSignPdf() {
        System.out.println("Path--->" + PDF_PATH);
        Integrator integrator = new Integrator();

        String configPath = new File("config").getAbsolutePath();
        integrator.setConfigPath(configPath);

        integrator.setDeclType("D390");

        String xmlFile = "xml/D390.xml";
        String errFile = "error.err";

        // Check if the PDF exists before attempting to sign it
        File pdfFile = new File(PDF_PATH);
        if (pdfFile.exists()) {
            String signedPdfFile = PDF_DIRECTORY + File.separator + "signed_output.pdf";
            String pin = "Account123";  // Demo PIN
            String smartCard = "aladdin";  // Using schlumberger as the demo smart card
            int signResult = integrator.signPdf(xmlFile, errFile, null, signedPdfFile, pin, smartCard);

            System.out.println("PDF signing result: " + signResult);
            System.out.println(integrator.getFinalMessage());

            if (signResult == 0) {
                // Validate signed PDF
                int signedPdfValidationResult = integrator.parseDocument(signedPdfFile, errFile);
                System.out.println("Signed PDF validation result: " + signedPdfValidationResult);
                System.out.println(integrator.getFinalMessage());

                if (signedPdfValidationResult == 0) {
                    // Show success alert
                    JOptionPane.showMessageDialog(null, "PDF successfully Signed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Failed to sign the PDF.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "PDF file not found at the specified path: " + PDF_PATH,
                    "File Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    
    private void pdfValidateSign() {
        System.out.println("Path--->" + PDF_PATH);
        Integrator integrator = new Integrator();

        String configPath = new File("config").getAbsolutePath();
        integrator.setConfigPath(configPath);

        integrator.setDeclType("D390");

        String xmlFile = "xml/D390.xml";
        String errFile = "error.err";
        int validationResult = integrator.parseDocument(xmlFile, errFile);
        System.out.println("XML Validation result--: " + validationResult);
        System.out.println(integrator.getFinalMessage());

        if (validationResult >= 0) {
            // Create PDF
            String pdfFile = "pdfs/output.pdf";  // Update path to the 'pdfs' directory
            int pdfCreationResult = integrator.pdfCreation(xmlFile, errFile, null, pdfFile);
            System.out.println("PDF creation result--: " + pdfCreationResult);
            System.out.println(integrator.getFinalMessage());

            if (pdfCreationResult == 0) {
                // Sign PDF using demo mode
                String pin = "Account123";  // Demo PIN
                String smartCard = "aladdin";  // Using schlumberger as the demo smart card
                String signedPdfFile = "pdfs/signed_output.pdf";  // Update path to the 'pdfs' directory
                int signResult = integrator.signPdf(xmlFile, errFile, null, signedPdfFile, pin, smartCard);

                System.out.println("PDF signing result: " + signResult);
                System.out.println(integrator.getFinalMessage());

                if (signResult == 0) {
                    // Validate signed PDF
                    int signedPdfValidationResult = integrator.parseDocument(signedPdfFile, errFile);
                    System.out.println("Signed PDF validation result: " + signedPdfValidationResult);
                    System.out.println(integrator.getFinalMessage());

                    if (signedPdfValidationResult == 0) {
                        // Show success alert
                        JOptionPane.showMessageDialog(null, "PDF successfully validated and signed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }
    }
    // Main method to run and display the UI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Folder Path Setup");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 400); 
            FolderPathSetupPage page = new FolderPathSetupPage(frame);
            frame.setContentPane(page.getPanel());
            frame.setVisible(true);
        });
    }
}
