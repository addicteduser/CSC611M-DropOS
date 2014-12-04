package dropos;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import message.DropOSProtocol;
import dropos.event.SynchronizationEvent;

public class DropClient extends Thread {
	private int port;
	private String hostname;
	private Socket clientSocket;
	private DropOSProtocol protocol;

	public DropClient() {
		hostname = Config.getIpAddress();
		port = Config.getPort();

		System.out.println("[SYSTEM] Created new CLIENT instance");

	}

	/**
	 * This method handles different kinds of events.
	 * 
	 * @param e
	 */
	private void eventPerformed(SynchronizationEvent e) {

		try {
			clientSocket = new Socket(hostname, port);
			System.out.println("[CLIENT] New event. I am now connecting to ["
					+ hostname + "] on port [" + port + "]");

			protocol = new DropOSProtocol(clientSocket);

			switch (e.getType()) {
			case ADD:
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
	 * <p>This method is called when the index list was able to identify files that were outdated. 
	 * It sends a packet header requesting for a file.</p>
	 * @param e this contains details of the file to be requested
	 */
	private void requestFile(SynchronizationEvent e) {
		
	}

	/**
	 * <p>This method recognizes that a file was now missing. The server must now be informed that the file is removed.
	 * It sends a packet header containing the command to delete the file.</p>
	 * 
	 * @param e this contains details of the file to be deleted
	 * @throws IOException
	 */
	private void deleteFile(SynchronizationEvent e) {
		File f = new File(Config.getPath() + "\\" + e.getFile().toString());
		
	}

	/**
	 * <p>This method recognizes that a file was newly added / needs to be updated.
	 * It sends a packet header and the payload.</p>
	 * 
	 * <p>
	 * <b>Note:</b> The recipient automatically closes the connection once the payload was successfully received.
	 * </p>
	 * @param e this contains details of the newly added file 
	 * @throws IOException
	 */
	private void addFile(SynchronizationEvent e) throws IOException {
		File f = new File(Config.getPath() + "\\" + e.getFile().toString());
		protocol.sendHeaderAndFile(e, f);
	}

	@SuppressWarnings("unchecked")
	public void run() {
		// Sanity check - Check if path is a folder

		Path clientPath = Config.getPath();
		try {
			Boolean isFolder = (Boolean) Files.getAttribute(clientPath,
					"basic:isDirectory", NOFOLLOW_LINKS);

			if (!isFolder)
				throw new IllegalArgumentException("Path: " + clientPath
						+ " is not a folder");

		} catch (IOException ioe) {
			// Folder does not exists
			ioe.printStackTrace();
		}

		System.out.println("[CLIENT] Watching path: " + clientPath);

		// We obtain the file system of the Path
		FileSystem fs = clientPath.getFileSystem();

		// We create the new WatchService using the new try() block
		try (WatchService service = fs.newWatchService()) {

			// We register the path to the service
			// We watch for creation events
			clientPath.register(service, ENTRY_CREATE, ENTRY_DELETE,
					ENTRY_MODIFY);

			// Start the infinite polling loop
			WatchKey key = null;
			while (true) {
				key = service.take();

				// Dequeueing events
				Kind<?> kind = null;
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					// Get the type of the event
					kind = watchEvent.kind();
					Path newPath = ((WatchEvent<Path>) watchEvent).context();

					// Create a directory event from what happened
					SynchronizationEvent directoryEvent = new SynchronizationEvent(newPath,
							kind);

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
