package dropos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DropServer extends Thread {
	
	private static ServerSocket serversocket;
	private static Socket server;
	private static DataInputStream in;
	private static DataOutputStream out;

	public DropServer(int port) throws IOException {
		serversocket = new ServerSocket(port);
		System.out.println("[SYSTEM] Starting File Sync Server...");
	}
	
	public void run() {
		while (true) {
			try {
				System.out.println("[SERVER] Waiting for client connections on port " + serversocket.getLocalPort() + "...");
				server = serversocket.accept();

				// wait for connections...

				// when a connection is made...
				System.out.println("[SYSTEM] SERVER just connected to CLIENT with IP:" + server.getInetAddress().toString());

				// instantiate connections to get input from CLIENT
				in = new DataInputStream(server.getInputStream());
				// instantiate connections to get input from CLIENT
				out = new DataOutputStream(server.getOutputStream());

				// CODE HERE

				// close connection
				server.close();			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
