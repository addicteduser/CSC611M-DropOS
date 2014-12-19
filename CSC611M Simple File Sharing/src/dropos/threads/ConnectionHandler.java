package dropos.threads;

import indexer.Index;
import indexer.Resolution;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import message.DropOSProtocol;
import message.FileAndMessage;
import message.Message;
import message.packet.PacketHeader;
import message.packet.UpdatePacketHeader;
import dropos.Config;
import dropos.DropCoordinator;
import dropos.DropServer;

/**
 * During initialization, the {@link DropServer} creates 16 {@link CoordinatorConnectionHandler} instances which block at the queue which holds the pending
 * {@link Socket} instances to be handled. Once a new instance is available, the {@link Socket} is received and is then handled.
 * 
 * <p>
 * <b>Note:</b> This class is meant to handle connections from a {@link DropCoordinator} to a {@link DropServer}.
 *
 */
public class ConnectionHandler extends Thread {
	private BlockingQueue<Socket> queue;
	private Socket connectionSocket;
	private DropOSProtocol protocol;
	private int port;

	public ConnectionHandler(BlockingQueue<Socket> queue, int port) {
		this.queue = queue;
		this.port = port;
		this.start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				connectionSocket = queue.take();
				if (isCoordinator(connectionSocket) == false)
				{
					connectionSocket.close();
					continue;
				}
				
				protocol = new DropOSProtocol(connectionSocket);
				
				log("Accepted connection from coordinator [" + protocol.getIPAddress() + "]");
				PacketHeader headers = protocol.receiveHeader();
				Message msg = headers.interpret(protocol);
				
				log("Received message from coordinator: " + msg.toString());
				interpretMessage(msg);
				System.out.println();

			} catch (IOException e) {
				log("The Coordinator has received the file.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns false when the IP address of the {@link Socket} is not the same with the coordinator as defined in the {@link Config} file.
	 * @param socket
	 * @return
	 */
	private boolean isCoordinator(Socket socket) {
		return socket.getInetAddress().toString().substring(1).equals(Config.getIpAddress());
	}

	private void interpretMessage(Message msg) throws UnknownHostException, IOException {
		String[] split = msg.message.split(":");
		String command = split[0];
		command = command.toUpperCase();
		System.out.println("COMMAND: " + command);

		FileAndMessage fileAndMsg = null;
		if (msg instanceof FileAndMessage){
			fileAndMsg = (FileAndMessage) msg;
		}
		
		switch (command) {
		case "INDEX":
			/**
			 * If the server receives an index command, it means that the client sent its index. What the server is supposed to do is to respond with its own
			 * index and perform resolution.
			 */
			respondWithIndex(fileAndMsg);
			break;
		case "REQUEST":
			respondToRequest(fileAndMsg);
			break;
		case "UPDATE":
			// Handled in interpret

			break;
		case "DELETE":
			// Handled in interpret
			break;
		}

	}

	private void respondToRequest(FileAndMessage msg) throws UnknownHostException, IOException {
		File f = msg.getFile();
		protocol = DropOSProtocol.connectToCoordinator();
		log("Created a new socket connection to the Coordinator.");

		log("Sending the requested file [" + f.getName() + "] as an update...");
		
		UpdatePacketHeader updatePacket = PacketHeader.createUpdate(f.getName(), f.length(), Config.getPort());
		
		protocol.sendFile(updatePacket, f);
		log("Sent.");
	}

	/**
	 * This method creates a new connection to the client, and sends the server's {@link Index}. Afterwards, it uses the client {@link Index} (which it just
	 * received) and its own server {@link Index} to perform {@link Resolution}.
	 * 
	 * @param msg
	 *            the message containing both the file and the message
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void respondWithIndex(FileAndMessage msg) throws UnknownHostException, IOException {
		log("About to send this server's index list to coordinator.");
		log("Connecting to the server with a new socket.");
		
		protocol = new DropOSProtocol(new Socket(protocol.getIPAddress(), Config.getPort()));

		log("Sending the server's index list.");
		// Respond by sending your own index
		protocol.sendIndex(port);
	}

	private void log(String message) {
		System.out.println("[Server " + port + "] " + message);
	}
}
