package dropos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;

public class DropClient extends Thread {
	private static int port;
	private static String hostname;
	private static Socket client;
	private static DataInputStream in;
	private static DataOutputStream out;
	private Path clientPath;
	
	public DropClient() {
		this.hostname = Config.getIpAddress();
		this.port = Config.getPort();
		this.clientPath = Config.getPath();
		System.out.println("[SYSTEM] Created new CLIENT instance");
		
		try {
			// create connection
			System.out.println("[CLIENT] CLIENT trying to connect to SERVER at IP:" + hostname + " on port " + port);
			client = new Socket(hostname, port);
			System.out.println("[CLIENT] CLIENT just connected to SERVER. CLIENT has IP:" + client.getInetAddress().toString());
			
			// instantiate connections to get input from CLIENT
			in = new DataInputStream(client.getInputStream());
			// instantiate connections to get input from CLIENT
			out = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		DirectoryWatcher.watchDirectoryPath(clientPath, out);
	}
	
}
