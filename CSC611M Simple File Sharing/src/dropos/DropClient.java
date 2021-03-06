package dropos;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
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
import java.util.ArrayList;

import message.DropOSProtocol;
import message.DropOSProtocol.HostType;
import message.FileAndMessage;
import message.packet.FilePacketHeader;
import message.packet.IndexListPacketHeader;
import message.packet.PacketHeader;
import message.packet.RequestPacketHeader;
import message.packet.UpdatePacketHeader;
import dropos.event.SynchronizationEvent;
import dropos.ui.DropClientWindow;

public class DropClient implements Runnable {
	private DropOSProtocol protocol;
	private ServerSocket serverSocket;
	private int port;
	private ArrayList<SynchronizationEvent> events;

	private DropClient(int port) throws IOException {
		this.port = port;
		serverSocket = new ServerSocket(port);
		events = new ArrayList<SynchronizationEvent>();
		log("Client is now listening on port " + port + ".");
	}

	/**
	 * This method is set to false from the GUI, which allows this thread to terminate.
	 */
	public static boolean RUNNING = true;

	public void run() {
		// Check offline changes
		Resolution compare = checkOfflineChanges();
		
		// Check if folder exists
		Path path = Config.getInstancePath(port);
		checkIfClientFolderExists(path);
		
		// Check if index exists
		Index.readMyIndex(port);


		// If changes exist, handle them
		if (compare.countChanges() > 0) {
			log("Offline changes detected: " + compare);
			handleResolution(compare);
		}

		// Create GUI
		new DropClientWindow();

		// Watch directory
		Path clientPath = Config.getInstancePath(port);
		watchDirectory(clientPath);
		
		// When the GUI is closed, the shutdown hook fires. The index is updated and written down.
		shutdownHook();
	}

	private Resolution checkOfflineChanges() {
		protocol = DropOSProtocol.connectToCoordinator();
		
		PacketHeader packet = PacketHeader.createClientRegister(port);
		protocol.sendMessage(packet);

		log("Producing index list from directory:");
		System.out.println("         " + Config.getInstancePath(port) + "\n");
		
		Index olderIndex = Index.startUp(port);
		Index newerIndex = Index.directory(port);

		return Resolution.compare(olderIndex, newerIndex);
	}
	

	private void checkIfClientFolderExists(Path path) {
		if (Files.notExists(path)) {
			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				log("Could not create a directory at the selected path.");
			}
		}
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

		log("Now watching the following directory for changes:");
		log("   " + clientPath);

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
					
					@SuppressWarnings("unchecked")
					Path newPath = ((WatchEvent<Path>) watchEvent).context();

					// Create a directory event from what happened
					Path file = Config.getInstancePath(port).resolve(newPath);
					SynchronizationEvent directoryEvent = new SynchronizationEvent(file, kind);
					
					if (events.contains(directoryEvent)){
						// log("Duplicate event being ignored: " + directoryEvent);
						continue; // This event has already been handled
					}
						
					
					events.add(directoryEvent);

					if (kind.toString().equalsIgnoreCase("modify"))
						continue;
					
					// Ignore changes to the index list
					if (file.toFile().getName().equalsIgnoreCase("indexlist.txt"))
						continue;
					
					BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
					String filename = file.toFile().getName();
					long lastModified = attributes.lastModifiedTime().toMillis();
					
					

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
	 * 
	 * @param compare
	 */
	private void handleResolution(Resolution compare) {
		for (String filename : compare.keySet()) {
			String action = compare.get(filename);

			// Since these changes were detected on this host, we must inform the coordinator to logically synchronize the folder.
			switch (action) {
			case "NONE":
				// Do nothing
				break;

			case "UPDATE":
				try {
					File file = new File(Config.getInstancePath(port) + "\\" + filename);
					Path path = file.toPath();
					BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
					long lastModified = attributes.lastModifiedTime().toMillis();
					
					Index instance = Index.getInstance(port);
					instance.put(filename, lastModified);
				} catch (Exception err) {
					log("Error, could not add " + filename + " to the index.");
				}
				break;

			case "REQUEST":
				break;

			case "DELETE":
				break;
			}
		}
		
		Index.getInstance(port).write(port);

		try {
			log("Sending the coordinator my index list.");
			protocol.sendIndex(port);
		} catch (Exception e) {
			log("Finished sending the index list.\n");
		}

		try {
			Socket connectionSocket = serverSocket.accept();
			
			protocol = new DropOSProtocol(connectionSocket);

			// Wait for a response (header... and later a file);
			// Note that we expect the coordinator to respond with an index list as well.
			IndexListPacketHeader phServerIndex = (IndexListPacketHeader) protocol.receiveHeader();
			

			// Receive the file once you have the packet header
			FileAndMessage message = (FileAndMessage) phServerIndex.interpret(protocol);
			log("Server connected and received their index list.");

			File f = message.getFile();

			// Perform resolution between coordinator and client
			Index serverIndex = Index.read(f);
			Index myIndex = Index.getInstance(port);

			Resolution resolution = Resolution.compare(serverIndex, myIndex);
			for (String filename : resolution.keySet()) {
				DropOSProtocol p = DropOSProtocol.connectToCoordinator();
				String action = resolution.get(filename);
				
				
				switch (action) {
				case "UPDATE":
					Path path = Config.getAbsolutePath().resolve(filename);
					long filesize = path.toFile().length();
					PacketHeader header = PacketHeader.createUpdate(filename, filesize, port, Index.getRawLastModified(f));
					p.sendFile(header, f);
					break;
				case "DELETE":
					Files.delete(Config.getInstancePath(port).resolve(filename));
					break;
				case "REQUEST":
					// Send file request to server
					RequestPacketHeader packet = PacketHeader.createRequest(filename, port);
					
					p.sendMessage(packet);

					// Wait for coordinator to send UPDATE message
					Socket s = serverSocket.accept();
					p = new DropOSProtocol(s);

					UpdatePacketHeader updateHeader = (UpdatePacketHeader) p.receiveHeader();
					
					// Interpret message and copy to actual folder destination
					phServerIndex.interpret(protocol);
					updateHeader.writeFile(port);
					break;
				}
			}

			log("Files updated.");

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
			log("New directory event [" + e.getType() + "] detected.");
			protocol = DropOSProtocol.connectToCoordinator();

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
			// Write on index again
			Index.getInstance(port).write(port);
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

		log("Now requesting for file: " + e.getFileName().toFile());
		PacketHeader requestPacket = PacketHeader.create(e, port);
		protocol.sendMessage(requestPacket);

		Socket connectionSocket = serverSocket.accept();
		protocol = new DropOSProtocol(connectionSocket);

		FilePacketHeader filePacketHeader = (FilePacketHeader) protocol.receiveHeader();

		filePacketHeader.interpret(protocol);
		log("File update packet header received from server.");
		
		filePacketHeader.writeFile(port);
		log("File received.");

	}

	/**
	 * <p>
	 * This method recognizes that a file was now missing. The coordinator must now be informed that the file is removed. It sends a packet header containing the
	 * command to delete the file.
	 * </p>
	 * 
	 * @param e
	 *            this contains details of the file to be deleted
	 * @throws IOException
	 */
	private void deleteFile(SynchronizationEvent e){
		PacketHeader deletePacket = PacketHeader.create(e, port);
		protocol.sendMessage(deletePacket);
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
		Path path = Config.getInstancePath(port);
		String filename = e.getFileName().toString();

		// Get attributes
		BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		long lastModified = attributes.lastModifiedTime().toMillis();

		Index directory = Index.getInstance(port);
		directory.put(filename, lastModified);

		File f = new File(path + "\\" + filename);
		protocol.performSynchronization(e, f, port);
	}

	public void log(String message) {
		System.out.println("[Client "+port+"] " + message);
	}

	/**
	 * Factory pattern to create a {@link DropClient} instance on the next available port. The function begins with the port dictated on the {@link Config}
	 * file.
	 * 
	 * This method is synchronized because the ports are resources that two {@link DropClient}s might get permissions for.
	 * By placing a mutex here, we ensure that only one {@link Host} is assigned to one port.
	 * 
	 * @return
	 */
	public synchronized static DropClient create() {
		boolean success = false;
		DropClient client = null;
		int port = Config.getPort();
		do {
			try {
				DropOSProtocol.type = HostType.Client;
				client = new DropClient(port);
				success = true;
			} catch (IOException e) {
				// log("Could not create a DropClient on port " + port + ". Attempting to use port " + (port + 1));
				++port;
			}
		} while (success == false);
		System.out.println("[Client] Successfully created a DropClient on port " + port);
		return client;
	}
	
	private void shutdownHook(){
		Index startUp = Index.startUp(port);
		Index now = Index.directory(port);

		Resolution resolution = Resolution.compare(startUp, now);
		if (resolution.countChanges() > 0)
			System.out.println(resolution);
		now.write(port);
	}
}
