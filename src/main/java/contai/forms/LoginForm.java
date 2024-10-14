package contai.forms;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import contai.SessionManager;

public class LoginForm {
	private static final Logger logger = Logger.getLogger(LoginForm.class);

    private JPanel panel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JFrame parentFrame;
    private JCheckBox rememberMeCheckbox;
    private Timer timer;
    private SessionManager sessionManager;
  
    public LoginForm(JFrame parentFrame,SessionManager sessionManager) {
    	this.sessionManager=sessionManager;
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
            JOptionPane.showMessageDialog(panel, "Internal error: Username and Password fields are not properly initialized.", "Login Error", JOptionPane.ERROR_MESSAGE);
            logger.error("Username or password field is null");
            return;
        }
        
        if (username.isEmpty() && password.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Please enter both username and password.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Please enter your username.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(panel, "Please enter your password.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
			sessionManager.login(username, password);     
        } catch (Exception e) {
            logger.error("Exception occurred during login", e);
            JOptionPane.showMessageDialog(panel, "An unexpected error occurred during login. Please check your internet connection and try again.", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}