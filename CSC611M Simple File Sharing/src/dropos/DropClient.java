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
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import message.DropOSProtocol;
import dropos.event.DirectoryEvent;

public class DropClient extends Thread {
	private int port;
	private String hostname;
	private Socket clientSocket;
	private DropOSProtocol protocol;

	public DropClient() {
		hostname = Config.getIpAddress();
		port = Config.getPort();

		System.out.println("[SYSTEM] Created new CLIENT instance");

		try {
			// create connection
			
			clientSocket = new Socket(hostname, port);
			System.out.println("[CLIENT] I am now connected to [" + hostname + "] on port [" + port + "]");
			
			protocol = new DropOSProtocol(clientSocket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method handles different kinds of events.
	 * @param e
	 */
	private void eventPerformed(DirectoryEvent e) {
		
		switch(e.getType()){
		case ADD:
			
			addFile(e);
			break;
		case DELETE:
			 
			break;
			 
		case MODIFY:
			
			break;
		}
	}

	private void addFile(DirectoryEvent event) {
		try {
			File f = new File(Config.getPath() + "\\" + event.getFile().toString());
			long size = Files.size(event.getFile());
			protocol.sendMessage("ADD " + size + " " + event.getFile());
			
			String message = protocol.receiveHeader();
			
			if (message.equalsIgnoreCase("GO")){
				protocol.sendFile(f);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
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

		System.out.println("[CLIENT] Watching path: " + clientPath);

		// We obtain the file system of the Path
		FileSystem fs = clientPath.getFileSystem();

		// We create the new WatchService using the new try() block
		try (WatchService service = fs.newWatchService()) {

			// We register the path to the service
			// We watch for creation events
			clientPath.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

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
					DirectoryEvent directoryEvent = new DirectoryEvent(newPath, kind);
					
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
