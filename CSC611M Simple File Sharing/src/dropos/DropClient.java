package dropos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DropClient extends Thread {
	private static int port;
	private static String hostname;
	private static Socket client;
	private static DataInputStream in;
	private static DataOutputStream out;
	
	public DropClient(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
		System.out.println("[SYSTEM] Created new CLIENT instance");
	}
	
	public void run() {
		try {
			// create connection
			System.out.println("[SYSTEM] CLIENT trying to connect to SERVER at IP:" + hostname + " on port " + port);
			client = new Socket(hostname, port);
			System.out.println("[SYSTEM] CLIENT just connected to SERVER. CLIENT has IP:" + client.getInetAddress().toString());
			
			// instantiate connections to get input from CLIENT
			in = new DataInputStream(client.getInputStream());
			// instantiate connections to get input from CLIENT
			out = new DataOutputStream(client.getOutputStream());
			
			// CODE HERE
			
			// close connection
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
