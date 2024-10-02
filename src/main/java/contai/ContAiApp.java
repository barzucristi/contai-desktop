package contai;

import javax.swing.*;

import contai.forms.FolderPathSetupPage;
import contai.forms.LoginForm;

public class ContAiApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ContAi");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);            
            LoginForm loginForm = new LoginForm(frame);
            frame.setContentPane(loginForm.getPanel());
            frame.setVisible(true);
        });
    }
}
