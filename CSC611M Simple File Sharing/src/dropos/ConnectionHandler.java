package dropos;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionHandler extends Thread {
	
	private static ServerSocket serversocket;
	private ThreadPool pool;
	public ConnectionHandler(int port) throws IOException {
		serversocket = new ServerSocket(port);
		pool = new ThreadPool(16);
	}
	
	public void run() {
		while (true) {
			try {
				System.out.println("[SERVER] Waiting for client connections on port " + serversocket.getLocalPort() + "...");
				Socket connectionSocket = serversocket.accept();
				
				pool.addTask(connectionSocket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
