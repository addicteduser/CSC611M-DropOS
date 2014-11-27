package dropos;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import dropos.event.DirectoryEvent;

public class DropClient extends Thread {
	private static int port;
	private static String hostname;
	private static Socket client;
	private static DataInputStream in;
	private static OutputStream serverOut;

	private static Path clientPath;

	public DropClient() {
		hostname = Config.getIpAddress();
		port = Config.getPort();
		clientPath = Config.getPath();

		System.out.println("[SYSTEM] Created new CLIENT instance");

		try {
			// create connection
			System.out.println("[CLIENT] CLIENT trying to connect to SERVER at IP:" + hostname + " on port " + port);
			
			client = new Socket(hostname, port);
			
			System.out.println("[CLIENT] CLIENT just connected to SERVER. CLIENT has IP:" + client.getInetAddress().toString());

			// instantiate connections to get input from CLIENT
			in = new DataInputStream(client.getInputStream());
			
			// instantiate connections to get input from CLIENT
			serverOut = client.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method handles different kinds of events.
	 * @param e
	 */
	private void eventPerformed(DirectoryEvent e) {
		
		// From the type of event, get the bytes to be sent
		try {
			byte[] raw = e.getBytes();
			serverOut.write(raw);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	/**
	 * This method can send files to the server.
	 * @param file
	 */
	private void sendFile(File file) {
		// File myFile = clientPath.resolve("test.txt").toFile();
		// sendFile(myFile);
		// while(true);
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] mybytearray = new byte[(int) file.length()];

			BufferedInputStream bin = new BufferedInputStream(fis);
			bin.read(mybytearray, 0, mybytearray.length);
			serverOut.write(mybytearray, 0, mybytearray.length);
			serverOut.flush();

			fis.close();
			client.close();
			System.out.println("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void run() {
		// Sanity check - Check if path is a folder
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
