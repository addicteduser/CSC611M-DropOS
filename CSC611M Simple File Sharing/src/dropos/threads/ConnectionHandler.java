package dropos.threads;

import indexer.Index;
import indexer.Resolution;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import message.DropOSProtocol;
import message.FileAndMessage;
import message.Message;
import message.PacketHeader;
import dropos.Config;
import dropos.DropCoordinator;
import dropos.DropServer;

/**
 * During initialization, a {@link CoordinatorConnectionHandler} is made to block at the queue which holds the pending {@link Socket} instances to be handled.
 * Once a new instance is available, the {@link Socket} is received and is then handled.
 * 
 * <p><b>Note:</b> This class is meant to handle connections from a {@link DropCoordinator} to a {@link DropServer}. 
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

				System.out.println("Server has accepted connection from coordinator [" + protocol.getIPAddress() + "]");

				PacketHeader headers = protocol.receiveHeader();
				Message msg = headers.interpret(protocol);
				interpretMessage(msg);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void interpretMessage(Message msg) throws UnknownHostException, IOException {
		String command = msg.message;
		command = command.toUpperCase();
		
		switch(command){
		case "INDEX":
			/**
			 *  If the server receives an index command, it means that the client sent its index.
			 *  What the server is supposed to do is to respond with its own index and perform resolution.
			 */
			respondWithIndex((FileAndMessage) msg);
			break;
		}
		
		
	}

	/**
	 * This method creates a new connection to the client, and sends the server's {@link Index}.
	 * Afterwards, it uses the client {@link Index} (which it just received) and its own server {@link Index} to perform {@link Resolution}. 
	 * @param msg the message containing both the file and the message
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void respondWithIndex(FileAndMessage msg) throws UnknownHostException, IOException {
		System.out.println("[Server] A new socket connection is being made...");
		protocol = new DropOSProtocol(new Socket(protocol.getIPAddress(), Config.getPort()));
		
		System.out.println("[Server] Sending the server's index list.");
		// Respond by sending your own index
		protocol.sendIndex();

		System.out.println("[Server] Performing resolution...");
		// Parse the client's index
		Index clientIndex = Index.read(msg.getFile());
		
		// Get your own index
		Index serverIndex = Index.getInstance();
		
		// Perform resolution afterwards
		Resolution resolution = Resolution.compare(serverIndex, clientIndex);
		
		System.out.println("[Server] These were the following changes "
				+ "received:\n" + resolution);	
	}

	// TODO This is supposed to check the server-side resolution if a file is indeed valid. If so, it should return true to accept the file.
	private boolean isValid(PacketHeader headers) {
		
		return false;
	}
}
