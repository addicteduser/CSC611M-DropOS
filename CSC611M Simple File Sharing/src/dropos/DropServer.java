package dropos;

import indexer.Index;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import message.DropOSProtocol;
import message.DropOSProtocol.HostType;
import message.packet.PacketHeader;
import dropos.threads.CoordinatorConnectionHandler;
import dropos.threads.ThreadPool;

/**
 * A {@link DropServer} is a simple application that waits for connections.
 * During initialization, it produces a thread pool of sixteen (16)
 * {@link CoordinatorConnectionHandler} instances which are blocked until a
 * connection is made.
 *
 */
public class DropServer implements Runnable {

	private static ServerSocket serverSocket;
	private ThreadPool pool;
	private DropOSProtocol protocol;
	private int port;

	private DropServer(int port) throws IOException {
		this.port = port;
		serverSocket = new ServerSocket(port);
		pool = new ThreadPool(port);
	}

	public void run() {
		protocol = DropOSProtocol.connectToCoordinator();
		
		PacketHeader serverRegister = PacketHeader.createServerRegister(port);
		protocol.sendMessage(serverRegister);


		// Check if folder exists
		Path path = Config.getInstancePath(port);
		checkIfServerFolderExists(path);
		
		// Check if index exists
		Index.readMyIndex(port);

		while (true) {
			try {
				Socket connectionSocket = serverSocket.accept();
				pool.addTask(connectionSocket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void checkIfServerFolderExists(Path path) {
		if (Files.notExists(path)) {
			log("Detected that server folder is not yet created. Creating one now at path:");
			log(path.toString());

			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				log("Could not create a directory at the selected path.");
			}
		}
	}

	
	/**
	 * Factory pattern to create a {@link DropServer} instance on the next
	 * available port. The function begins with the port dictated on the
	 * {@link Config} file.
	 * 
	 * This method is synchronized because the ports are resources that two {@link DropServer}s might get permissions for.
	 * By placing a mutex here, we ensure that only one {@link Host} is assigned to one port.
	 * 
	 * @return
	 */
	public synchronized static DropServer create() {
		boolean success = false;
		DropServer server = null;
		int port = Config.getPort();
		do {
			try {
				DropOSProtocol.type = HostType.Server;
				server = new DropServer(port);
				success = true;
			} catch (IOException e) {
				// log("Could not create client on port " + port + ". Attempting to use port " + (port + 1));
				++port;
			}
		} while (success == false);
		log("Successfully created a DropServer on port " + port);
		return server;
	}

	private static void log(String message) {
		System.out.println("[Server] " + message);

	}
}
