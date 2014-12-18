package dropos;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import message.DropOSProtocol;
import message.DropOSProtocol.HostType;
import dropos.threads.CoordinatorConnectionHandler;
import dropos.threads.ThreadPool;

/**
 * A {@link DropServer} is a simple application that waits for connections. During initialization, it produces a thread pool of sixteen (16)
 * {@link CoordinatorConnectionHandler} instances which are blocked until a connection is made.
 *
 */
public class DropServer implements Runnable{

	private static ServerSocket serverSocket;
	private ThreadPool pool;
	private DropOSProtocol protocol;

	private DropServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		pool = new ThreadPool(16);
	}

	public void run() {
		try {
			protocol = new DropOSProtocol();
			protocol.sendMessage("REGISTER");
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		}
		
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
	

	/**
	 * Factory pattern to create a {@link DropServer} instance on the next available port. The function begins with the port dictated on the {@link Config} file.
	 * @return
	 */
	public static DropServer create() {
		boolean success = false;
		DropServer server = null;
		int port = Config.getPort();
		do {
			try {
				DropOSProtocol.type = HostType.Server;
				server = new DropServer(port);
				success = true;
			} catch (IOException e) {
				System.out.println("Could not create client on port " + port + ". Attempting to use port " + (port + 1));
				++port;
			}
		} while (success == false);
		System.out.println("Successfully created a DropServer on port " + port);
		return server;
	}
}
