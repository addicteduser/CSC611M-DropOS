package dropos;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import dropos.threads.CoordinatorConnectionHandler;
import dropos.threads.CoordinatorThreadPool;

/**
 * <p>The {@link DropCoordinator} has a similar structure to the {@link DropServer} class. It has a thread pool that can handle
 * sixteen (16) connections from clients at the same time.</p>
 * 
 * <p>Once a connection is made, a {@link CoordinatorConnectionHandler} handles the connection.
 * @author Darren
 *
 */
public class DropCoordinator {
	private static ServerSocket serverSocket;
	private CoordinatorThreadPool pool;

	public DropCoordinator(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		pool = new CoordinatorThreadPool(16);
	}

	public void run() {
		while (true) {
			try {
				System.out.println("[Coordinator] Waiting for connections on port " + serverSocket.getLocalPort() + "...");
				Socket connectionSocket = serverSocket.accept();

				pool.addTask(connectionSocket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
