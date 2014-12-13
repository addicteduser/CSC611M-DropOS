package dropos;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import indexer.Index;
import indexer.Resolution;

import java.io.File;
import java.io.IOException;
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
import message.IndexListPacketHeader;
import dropos.event.SynchronizationEvent;
import dropos.ui.DropClientWindow;

public class DropClient {
	private DropOSProtocol protocol;

	/**
	 * This method is set to false from the GUI, which allows this thread to terminate.
	 */
	public static boolean RUNNING = true;

	public DropClient() {
		System.out.println("[Client] Initializing the client.");

		System.out.println("[Client] Connecting to the server...\n");
		// Create a connection with the server
		try {
			protocol = new DropOSProtocol();
		} catch (IOException e) {
			System.err.println("Cannot create connection to server.");
		}

		System.out.println("[Client] Producing index list from directory:");
		System.out.println("         " + Config.getPath().toString() + "\n");

		Index olderIndex = Index.startUp();
		Index newerIndex = Index.directory();

		Resolution compare = Resolution.compare(olderIndex, newerIndex);

		if (compare.countChanges() > 0) {
			System.out.println("[Client] Here are the offline changes detected: " + compare);
			System.out.println("About to update server regarding offline changes...");
			handleResolution(compare);
		} else {
			System.out.println("[Client] There were no offline changes detected.");
		}
		System.out.println();

		new DropClientWindow();

	}

	private void handleResolution(Resolution compare) {
		try {
			// Send your index file
			protocol.sendIndex();
			while(protocol.isFinished() == false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			protocol = new DropOSProtocol();
			
			// Wait for a response (header... and later a file);
			// Note that we expect the server to respond with an index list as well.
			IndexListPacketHeader phServerIndex = (IndexListPacketHeader) protocol.receiveHeader();

			// Receive the file once you have the packet header
			File serverIndex = phServerIndex.receiveFile(protocol);

			// TODO: perform resolution here

			for (String filename : compare.keySet()) {
				String action = compare.get(filename);

				// Since these changes were detected on this host, we must inform the server to logically synchronize the folder.
				switch (action) {
				case "NONE":
					// Do nothing
					break;

				case "UPDATE":

					break;

				case "REQUEST":
					break;

				case "DELETE":
					break;
				}

			}

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

			System.out.println("[Client] New event. Connecting to the server...");

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
	 */
	private void requestFile(SynchronizationEvent e) {

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

		File f = new File(path + "\\" + filename);
		protocol.performSynchronization(e, f);

		// Get attributes
		BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		long lastModified = attributes.lastModifiedTime().toMillis();

		Index directory = Index.getInstance();
		directory.put(filename, lastModified);
	}

	@SuppressWarnings("unchecked")
	public void run() {
		// Sanity check - Check if path is a folder

		Path clientPath = Config.getPath();
		try {
			Boolean isFolder = (Boolean) Files.getAttribute(clientPath, "basic:isDirectory", NOFOLLOW_LINKS);

			if (!isFolder)
				throw new IllegalArgumentException("Path: " + clientPath + " is not a folder");

		} catch (IOException ioe) {
			// Folder does not exists
			ioe.printStackTrace();
		}

		System.out.println("[Client] Now watching the directory for changes.");

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
					SynchronizationEvent directoryEvent = new SynchronizationEvent(newPath, kind);

					if (kind.toString().equalsIgnoreCase("modify"))
						continue;

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

}
