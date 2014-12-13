package dropos.ui;

import indexer.Index;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;

import dropos.DropClient;

public class DropClientWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3216122212731392882L;

	
	public DropClientWindow() {
		super("DropClient");
		setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
		
		JButton btnExit = new JButton("Exit and index directory");
		btnExit.setFocusable(false);
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitApplicationProperly();
			}
		});
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exitApplicationProperly();
				super.windowClosing(e);
			}
		});
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(250, 75);
		setLocationRelativeTo(null);
		add(btnExit);
		setVisible(true);
	}
	
	private void exitApplicationProperly(){
		DropClient.RUNNING = false;
	}
}
