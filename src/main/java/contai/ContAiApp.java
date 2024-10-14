package contai;

import javax.swing.*;

public class ContAiApp {
	public static void main(String[] args) {
	    SwingUtilities.invokeLater(() -> {
	        JFrame frame = new JFrame("ContAi");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

	        ImageIcon titleIcon = new ImageIcon(ContAiApp.class.getResource("/images/ai.png"));
            frame.setIconImage(titleIcon.getImage());
            
	        SessionManager sessionManager = new SessionManager(frame);
	        sessionManager.initializeSession();

	        frame.setVisible(true);
	    });
	    
	    
	}
}
