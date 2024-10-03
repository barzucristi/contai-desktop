package contai.forms;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import com.google.gson.JsonObject;

public class FolderPathPage {

    private JPanel panel;
    private JFrame parentFrame;
 
    public FolderPathPage(JFrame parentFrame) {
        this.parentFrame = parentFrame;
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
        gbc1.insets = new Insets(50, 50, 50, 50); // Set 50-pixel margin around folder panels
        gbc1.fill = GridBagConstraints.HORIZONTAL;

        JPanel folder1 = createFolderSelectionPanel("Windows//Disk D://My Files//Folder1", "/images/bi_folder.png", false);
        JPanel folder2 = createFolderSelectionPanel("Windows//Disk D://My Files//Folder2", "/images/bi_folder_change.png", true);
        JPanel folder3 = createFolderSelectionPanel("Windows//Disk D://My Files//Folder3", "/images/bi_folder_change.png", true);

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

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);
        buttonPanel.add(customPathButton);

        mainGbc.gridy = 4;
        panel.add(buttonPanel, mainGbc);

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
