package dropos;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class DropServer extends Thread {
	
	private static ServerSocket serversocket;
	private ArrayList<ClientConnection> clients;
	
	public DropServer(int port) throws IOException {
		serversocket = new ServerSocket(port);
		clients = new ArrayList<ClientConnection>();
	}
	
	public void run() {
		while (true) {
			try {
				System.out.println("[SERVER] Waiting for client connections on port " + serversocket.getLocalPort() + "...");
				Socket connectionSocket = serversocket.accept();
				
				ClientConnection clientConnection = new ClientConnection(connectionSocket);
				clientConnection.start();
				clients.add(clientConnection);
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
