package contai.forms;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class FolderPathPage {

    private JPanel panel;
    private JFrame parentFrame;
    private String username;
    private String hotFolderPath;
    private String signedDocsFolderPath;
    private String spvDocsFolderPath;
   
 
    public FolderPathPage(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.username = System.getProperty("user.name");
        this.hotFolderPath = "C:\\Users\\" + username + "\\Desktop\\Declaratii nesemnate";
        this.signedDocsFolderPath = "C:\\Documents\\contai\\declaratii semnate";
        this.spvDocsFolderPath = "C:\\Documents\\contai\\documente SPV";
        createFolders();
    }

    private void createFolders() {
        createFolder(hotFolderPath);
        createFolder(signedDocsFolderPath);
        createFolder(spvDocsFolderPath);
        secureHotFolder();
    }

    private void createFolder(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            try {
                if (folder.mkdirs()) {
                    System.out.println("Folder created: " + path);
                } else {
                    System.out.println("Failed to create folder: " + path);
                    JOptionPane.showMessageDialog(parentFrame, "Failed to create folder: " + path, "Folder Creation Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                System.out.println("Exception while creating folder: " + e.getMessage());
            }
        } else {
            System.out.println("Folder already exists: " + path);
        }
    }


    private void secureHotFolder() {
        try {
            File hotFolder = new File(hotFolderPath);
            

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                String command = "icacls \"" + hotFolderPath + "\" /inheritance:r /grant:r \"Users:R\" /T";
                Process process = Runtime.getRuntime().exec(command);
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    System.out.println("Hot folder secured successfully.");
                } else {
                    throw new Exception("icacls failed with exit code: " + exitCode);
                }
            } else {
                Files.setPosixFilePermissions(hotFolder.toPath(), PosixFilePermissions.fromString("r-xr-xr-x"));
                System.out.println("Hot folder secured successfully on Unix.");
            }
        } catch (Exception e) {
            System.out.println("Failed to secure hot folder: " + e.getMessage());
            JOptionPane.showMessageDialog(parentFrame,
                "Unable to fully secure the hot folder. Please ensure you have the necessary permissions.\n" +
                "The application will continue, but the hot folder may be editable.",
                "Security Warning",
                JOptionPane.WARNING_MESSAGE);
        }
    }


    public JPanel getPanel() {
        panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JLabel logo = new JLabel();
        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/images/ai.png"));
        logo.setIcon(new ImageIcon(logoIcon.getImage().getScaledInstance(70, 50, Image.SCALE_SMOOTH)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(25, 25, 25, 25); 
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(logo, gbc);

        JLabel titleLabel = new JLabel("Setup your Folder Paths");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(Color.decode("#333333"));

        JLabel descriptionLabel = new JLabel("Choose the best folder path which suits you the best");
        descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descriptionLabel.setForeground(Color.decode("#555555"));

        // Folder icons with paths
        JPanel folderPanel = new JPanel(new GridBagLayout());
        folderPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(50, 50, 50, 50); 
        gbc1.fill = GridBagConstraints.HORIZONTAL;

        JPanel folder1 = createFolderSelectionPanel(hotFolderPath, "/images/bi_folder.png", false);
        JPanel folder2 = createFolderSelectionPanel(signedDocsFolderPath, "/images/bi_folder_change.png", true);
        JPanel folder3 = createFolderSelectionPanel(spvDocsFolderPath, "/images/bi_folder_change.png", true);

        gbc1.gridx = 0;
        gbc1.gridy = 0;
        folderPanel.add(folder1, gbc1);

        gbc1.gridx = 1;
        folderPanel.add(folder2, gbc1);

        gbc1.gridx = 2;
        folderPanel.add(folder3, gbc1);

       
        JButton saveButton = new JButton("Save");
        saveButton.setBackground(Color.decode("#2979FF"));
        saveButton.setForeground(Color.WHITE);
        Border outerBorder = BorderFactory.createLineBorder(Color.decode("#2979FF"), 2); 
        Border saveInnerBorder = new EmptyBorder(10,50,10,50); 
        saveButton.setBorder(BorderFactory.createCompoundBorder(outerBorder, saveInnerBorder));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

      
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/solar_folder-path-connect-broken.png"));
        Image scaledImage = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        ImageIcon folderIcon = new ImageIcon(scaledImage);

        JButton customPathButton = new JButton("Custom Path", folderIcon);
        customPathButton.setBackground(Color.WHITE);
        customPathButton.setForeground(Color.decode("#2979FF"));
        customPathButton.setBorder(BorderFactory.createLineBorder(Color.decode("#2979FF"), 2));
        customPathButton.setHorizontalTextPosition(SwingConstants.RIGHT); 
        customPathButton.setVerticalTextPosition(SwingConstants.CENTER); 
        Border customPathInnerBorder = new EmptyBorder(8,20,8,20); 
        customPathButton.setBorder(BorderFactory.createCompoundBorder(outerBorder, customPathInnerBorder));
        customPathButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

      
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);
        buttonPanel.add(customPathButton);

      
        GridBagConstraints buttonPanelGbc = new GridBagConstraints();
        buttonPanelGbc.insets = new Insets(100, 0, 0, 0); 
        buttonPanelGbc.gridx = 0;
        buttonPanelGbc.gridy = 4; 
        buttonPanelGbc.gridwidth = 2;

       
        panel.add(buttonPanel, buttonPanelGbc);

      
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.insets = new Insets(5, 0, 5, 0);
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        panel.add(logo, mainGbc);

        mainGbc.gridy = 1;
        panel.add(titleLabel, mainGbc);

        mainGbc.gridy = 2;
        panel.add(descriptionLabel, mainGbc);

        mainGbc.gridy = 3;
        panel.add(folderPanel, mainGbc);

        return panel;
    }

    private JPanel createFolderSelectionPanel(String path, String iconPath, boolean isEditable) {
        JPanel folderPanel = new JPanel(new GridBagLayout());
        folderPanel.setBackground(Color.WHITE);

        ImageIcon originalIcon = new ImageIcon(getClass().getResource(iconPath));
        Image scaledImage = originalIcon.getImage().getScaledInstance(119, 143, Image.SCALE_SMOOTH);
        JLabel folderIcon = new JLabel(new ImageIcon(scaledImage));

        JTextField pathField = new JTextField(path);
        pathField.setEditable(isEditable); 
        pathField.setPreferredSize(new Dimension(200, 30));

      
        if (isEditable) {
            folderIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ImageIcon clickedIcon = new ImageIcon(originalIcon.getImage().getScaledInstance(122, 145, Image.SCALE_SMOOTH));
                    folderIcon.setIcon(clickedIcon);

                   
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int option = fileChooser.showOpenDialog(parentFrame);
                    if (option == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        pathField.setText(file.getAbsolutePath());
                    } else {
                        folderIcon.setIcon(new ImageIcon(scaledImage));
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    folderIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            });
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        folderPanel.add(folderIcon, gbc);

        gbc.gridy = 1;
        folderPanel.add(pathField, gbc);

        return folderPanel;
    }



    public static void main(String[] args) {
        JFrame frame = new JFrame("FolderPathPage");
        FolderPathPage folderPathPage = new FolderPathPage(frame);
        frame.setContentPane(folderPathPage.getPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setVisible(true);
    }
}
