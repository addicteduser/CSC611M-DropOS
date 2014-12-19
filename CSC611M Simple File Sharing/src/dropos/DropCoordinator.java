package dropos;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

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
public class DropCoordinator implements Runnable{
	private static DropCoordinator instance;
	private static ServerSocket serverSocket;
	private CoordinatorThreadPool pool;

	public DropCoordinator(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		pool = new CoordinatorThreadPool();
	}

	public void run() {
		Path path = Config.getInstancePath(Config.getPort());
		checkIfServerFolderExists(path);
		
		while (true) {
			try {
				log("Waiting for connections on port " + serverSocket.getLocalPort() + "...");
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

	private static void log(String message) {
		log("[Coordinator] " + message);
	}

	public static DropCoordinator create(){
		if (instance == null){
			try {
				instance = new DropCoordinator(Config.getPort());
			} catch (IOException e) {
				log("The coordinator could not run because it is not using port " + Config.getPort() +". Please start the coordinator first.");
				System.exit(1);
			}
		}else{
			log("The coordinator is already instantiated. Returning reference...");
		}
		return instance;
	}
}
