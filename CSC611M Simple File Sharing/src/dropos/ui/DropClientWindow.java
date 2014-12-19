package dropos.ui;

import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

import dropos.DropClient;

public class DropClientWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3216122212731392882L;

	public DropClientWindow() {
		super("DropClient");
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
		
		JLabel lblExit = new JLabel("Exit this window to stop the system.");
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitApplicationProperly();
				super.windowClosing(e);
			}
		});
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(250, 75);
		setLocationRelativeTo(null);
		add(lblExit);
		setVisible(true);
	}
	
	private void exitApplicationProperly(){
		DropClient.RUNNING = false;
	}
}
