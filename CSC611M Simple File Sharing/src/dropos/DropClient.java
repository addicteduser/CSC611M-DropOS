package dropos;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import indexer.FileAndLastModifiedPair;
import indexer.Index;
import indexer.Resolution;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;

import message.DropOSProtocol;
import message.FileAndMessage;
import message.FilePacketHeader;
import message.IndexListPacketHeader;
import message.PacketHeader;
import message.RequestPacketHeader;
import message.DropOSProtocol.HostType;
import dropos.event.SynchronizationEvent;
import dropos.ui.DropClientWindow;

public class DropClient implements Runnable{
	private DropOSProtocol protocol;
	private ServerSocket serverSocket;
	private int port;
	
	private DropClient(int port) throws IOException{
		this.port = port;
		serverSocket = new ServerSocket(port);
		System.out.println("[CLIENT] Client is now listening on port " + port + ".");
	}

	/**
	 * This method is set to false from the GUI, which allows this thread to terminate.
	 */
	public static boolean RUNNING = true;


	public void run() {
		// Check offline changes
		Resolution compare = checkOfflineChanges();
		
		// If changes exist, handle them
		if (compare.countChanges() > 0) {
			System.out.println("[CLIENT] Here are the offline changes detected: " + compare);
			System.out.println("[CLIENT] About to update server regarding offline changes...");
			handleResolution(compare);
		} else {
			System.out.println("[CLIENT] There were no offline changes detected.");
		}
		
		// Create GUI
		new DropClientWindow();
		
		// Watch directory
		Path clientPath = Config.getPath();
		watchDirectory(clientPath);
	}


	private Resolution checkOfflineChanges() {
		try {
			protocol = new DropOSProtocol();
			protocol.sendMessage("REGISTER:" + port);
		} catch (IOException e) {
			System.out.println("[Server] Now registered to the coordinator.");
		}
		
		
		System.out.println("[CLIENT] Connecting to the server...\n");
		// Create a connection with the server
		try {
			protocol = new DropOSProtocol();
		} catch (IOException e) {
			System.err.println("[CLIENT] Fatal error: cannot create connection to server. Now exiting...");
			System.exit(1);
		}

		System.out.println("[CLIENT] Producing index list from directory:");
		System.out.println("         " + Config.getPath().toString() + "\n");

		Index olderIndex = Index.startUp();
		Index newerIndex = Index.directory();

		return Resolution.compare(olderIndex, newerIndex);		
	}
	
	private void watchDirectory(Path clientPath) {
		// Sanity check - Check if path is a folder
				try {
					Boolean isFolder = (Boolean) Files.getAttribute(clientPath, "basic:isDirectory", NOFOLLOW_LINKS);

					if (!isFolder)
						throw new IllegalArgumentException("Path: " + clientPath + " is not a folder");

				} catch (IOException ioe) {
					// Folder does not exists
					ioe.printStackTrace();
				}

				System.out.println("[CLIENT] Now watching the directory for changes.");

				// We obtain the file system of the Path
				FileSystem fs = clientPath.getFileSystem();

				// We create the new WatchService using the new try() block
				try (WatchService service = fs.newWatchService()) {

					// We register the path to the service
					// We watch for creation events
					clientPath.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

					// Start the infinite polling loop
					WatchKey key = null;
					while (RUNNING) {
						key = service.take();

						// Dequeueing events
						Kind<?> kind = null;
						for (WatchEvent<?> watchEvent : key.pollEvents()) {
							// Get the type of the event
							kind = watchEvent.kind();
							Path newPath = ((WatchEvent<Path>) watchEvent).context();
							
							// Create a directory event from what happened
							SynchronizationEvent directoryEvent = new SynchronizationEvent(Config.getPath().resolve(newPath), kind);

							if (kind.toString().equalsIgnoreCase("modify"))
								continue;
							System.out.println("KIND: " + kind.toString());
							// Fire the event
							eventPerformed(directoryEvent);

						}

						if (!key.reset()) {
							break; // loop
						}
					}

				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
	}

	
	/**
	 * Given a {@link Resolution} instance, the client sends various messages to the coordinator as needed. 
	 * @param compare
	 */
	private void handleResolution(Resolution compare) {
		for (String filename : compare.keySet()) {
			String action = compare.get(filename);

			// Since these changes were detected on this host, we must inform the server to logically synchronize the folder.
			switch (action) {
			case "NONE":
				// Do nothing
				break;

			case "UPDATE":
				try {
					long dateModified = new File(Config.getPath() + "\\" + filename).length(); 
					FileAndLastModifiedPair e = new FileAndLastModifiedPair(filename, dateModified);
					Index.getInstance().add(e);	
				}catch(Exception err){
					System.out.println("Error, could not add " + filename + " to the index.");	
				}
				break;

			case "REQUEST":
				break;

			case "DELETE":
				break;
			}

		}
		
		
		try {
			System.out.println("[CLIENT] Sending the server my own index list.");
			protocol.sendIndex();
		} catch (Exception e) {
			System.out.println("[CLIENT] Finished sending the index list.\n");
		}
		
		
		try {
			System.out.println("[CLIENT] Now waiting for server to connect and send Server index list.");
			Socket connectionSocket = serverSocket.accept();
			protocol = new DropOSProtocol(connectionSocket);
			
			// Wait for a response (header... and later a file);
			// Note that we expect the server to respond with an index list as well.
			IndexListPacketHeader phServerIndex = (IndexListPacketHeader) protocol.receiveHeader();
			
			
			// Receive the file once you have the packet header
			FileAndMessage message = (FileAndMessage)phServerIndex.interpret(protocol);
			
			File f = message.getFile();
			
			
			// Perform resolution between server and client
			Index serverIndex = Index.read(f);
			Index myIndex = Index.getInstance();
			
			Resolution resolution = Resolution.compare(serverIndex, myIndex);
			for (String filename : resolution.keySet()) {
				DropOSProtocol p = new DropOSProtocol();
				String action = resolution.get(filename);
				switch(action){
				case "UPDATE":
					Long size = new File(filename).length();
					PacketHeader header = PacketHeader.create("UPDATE:"+size+":"+filename);
					p.sendFile(header ,f);
					break;
				case "DELETE":
					Files.delete(Config.getPath().resolve(filename));
					break;
				case "REQUEST":
					// Send file request to server
					p.sendMessage("REQUEST:"+filename);
					serverSocket = new ServerSocket(Config.getPort());
					
					// Wait for server to send UPDATE message
					Socket s = serverSocket.accept();
					p = new DropOSProtocol(s);
					
					FilePacketHeader requestHeader = (FilePacketHeader)p.receiveHeader();
					// Interpret message and copy to actual folder destination
					FileAndMessage requestMessage = (FileAndMessage)phServerIndex.interpret(protocol);
					requestHeader.writeFile();
					break;
				}
			}
			
			System.out.println("[CLIENT] Server index list received.");

			
			
				} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	/**
	 * This method handles different kinds of events.
	 * 
	 * @param e
	 */
	private void eventPerformed(SynchronizationEvent e) {
		try {

			System.out.println("[CLIENT] New event. Connecting to the server...");
			System.out.println("Event Type: " + e.getType().toString());
			protocol = new DropOSProtocol();

			switch (e.getType()) {
			case UPDATE:
				addFile(e);
				break;
			case DELETE:
				deleteFile(e);
				break;

			case REQUEST:
				requestFile(e);
				break;
			}
		} catch (IOException err) {
			err.printStackTrace();
		}
	}

	/**
	 * <p>
	 * This method is called when the index list was able to identify files that were outdated. It sends a packet header requesting for a file.
	 * </p>
	 * 
	 * @param e
	 *            this contains details of the file to be requested
	 * @throws IOException 
	 */
	private void requestFile(SynchronizationEvent e) throws IOException {
		System.out.println("[CLIENT] Now requesting for file: " + e.getFile().toFile());
		protocol.sendMessage("REQUEST:"+ e.getFile().toFile());
		
		System.out.println("[CLIENT] Waiting to get the requested file.");
		Socket connectionSocket = serverSocket.accept();
		protocol = new DropOSProtocol(connectionSocket);
		
		// Wait for a response (header... and later a file);
		// Note that we expect the server to respond with an index list as well.
		try {
			RequestPacketHeader rph = (RequestPacketHeader) protocol.receiveHeader();
			

			System.out.println("[CLIENT] Request packet header received.");
			rph.interpret(protocol);
			
			System.out.println("[CLIENT] File received.");
		}catch(Exception err){
			err.printStackTrace();
		}
	}

	/**
	 * <p>
	 * This method recognizes that a file was now missing. The server must now be informed that the file is removed. It sends a packet header containing the
	 * command to delete the file.
	 * </p>
	 * 
	 * @param e
	 *            this contains details of the file to be deleted
	 * @throws IOException
	 */
	private void deleteFile(SynchronizationEvent e) throws IOException {
		File f = new File(Config.getPath() + "\\" + e.getFile().toString());
		Files.delete(f.toPath());
	}

	/**
	 * <p>
	 * This method recognizes that a file was newly added / needs to be updated. It sends a packet header and the payload.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> The recipient automatically closes the connection once the payload was successfully received.
	 * </p>
	 * 
	 * @param e
	 *            this contains details of the newly added file
	 * @throws IOException
	 */
	private void addFile(SynchronizationEvent e) throws IOException {
		Path path = Config.getPath();
		String filename = e.getFile().toString();

		// Get attributes
		BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		long lastModified = attributes.lastModifiedTime().toMillis();

		Index directory = Index.getInstance();
		directory.put(filename, lastModified);
		
		File f = new File(path + "\\" + filename);
		System.out.println("UPDATE FILE: " + f.toPath());
		try {
			protocol.performSynchronization(e, f);
		} catch(IOException ex) {
			System.out.println("[CLIENT] The server received the file.");
		}
	}

	
	/**
	 * Factory pattern to create a {@link DropClient} instance on the next available port. The function begins with the port dictated on the {@link Config} file.
	 * @return
	 */
	public static DropClient create() {
		boolean success = false;
		DropClient client = null;
		int port = Config.getPort();
		do {
			try {
				DropOSProtocol.type = HostType.Server;
				client = new DropClient(port);
				success = true;
			} catch (IOException e) {
				System.out.println("Could not create client on port " + port + ". Attempting to use port " + (port + 1));
				++port;
			}
		} while (success == false);
		System.out.println("Successfully created a DropClient on port " + port);
		return client;
	}
}
