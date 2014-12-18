package dropos;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import dropos.threads.CoordinatorConnectionHandler;
import dropos.threads.ThreadPool;

/**
 * A {@link DropServer} is a simple application that waits for connections. During initialization, it produces a thread pool of sixteen (16)
 * {@link CoordinatorConnectionHandler} instances which are blocked until a connection is made.
 *
 */
public class DropServer {

	private static ServerSocket serverSocket;
	private ThreadPool pool;

	public DropServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		pool = new ThreadPool(16);
	}

	public void run() {
		while (true) {
			try {
				System.out.println("[SERVER] Waiting for client connections on port " + serverSocket.getLocalPort() + "...");
				Socket connectionSocket = serverSocket.accept();

				pool.addTask(connectionSocket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
