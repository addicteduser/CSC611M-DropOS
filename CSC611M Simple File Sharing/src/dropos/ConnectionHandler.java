package dropos;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import message.DropOSProtocol;

/**
 * During initialization, a {@link ConnectionHandler} is made to block at the queue which holds the pending {@link Socket} instances to be handled.
 * Once a new instance is available, the {@link Socket} is received and is then handled.  
 *
 */
public class ConnectionHandler extends Thread {
	private BlockingQueue<Socket> queue;
	private Socket connectionSocket;
	private DropOSProtocol protocol;

	public ConnectionHandler(BlockingQueue<Socket> queue) {
		this.queue = queue;
		this.start();
	}

	@Override
	public void run() {
		while (true) {
			try {

				this.connectionSocket = queue.take();
				protocol = new DropOSProtocol(connectionSocket);

				System.out.println("Server has accepted connection from client [" + protocol.getIPAddress() + "]");

				String headers = protocol.receiveHeader();
				interpretHeader(headers);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * <p>This method interprets the <b>packet headers</b> or the <b>message</b> so that the appropriate actions can be performed.</p>
	 * @param header
	 * @throws IOException
	 */
	private void interpretHeader(String header) throws IOException {
		String command = header.split(" ")[0];
		String params = header.substring(command.length() + 1).trim();

		switch (command) {
		case "ADD":
			addFile(params);
			break;

		case "MODIFY":
			modifyFile(params);
			break;

		case "DELETE":
			deleteFile(params);
			break;
		}
	}

	private void modifyFile(String params) {

	}

	private void deleteFile(String params) {
		// deleteIfExists(Path of the file)
	}

	private void addFile(String params) throws IOException {
		String fileSize = params.split(" ")[0];
		String fileName = params.substring(fileSize.length() + 1).trim();
		long size = Long.valueOf(fileSize);
		protocol.receiveFile(fileName, size);
	}
}
