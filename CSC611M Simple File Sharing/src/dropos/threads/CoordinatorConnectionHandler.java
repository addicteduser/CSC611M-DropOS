package dropos.threads;

import indexer.Index;
import indexer.Resolution;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import message.DropOSProtocol;
import message.FileAndMessage;
import message.FilePacketHeader;
import message.IndexListPacketHeader;
import message.Message;
import message.PacketHeader;
import dropos.Config;
import dropos.DropCoordinator;
import dropos.DropServer;

/**
 * During initialization, a {@link ConnectionHandler} is made to block at the queue which holds the pending {@link Socket} instances to be handled.
 * Once a new instance is available, the {@link Socket} is received and is then handled.
 * 
 * <p><b>Note:</b> This class is meant to handle connections from a {@link DropCoordinator} to a {@link DropServer}. 
 *
 */
public class CoordinatorConnectionHandler extends Thread {
	private BlockingQueue<Socket> queue;
	private Socket connectionSocket;
	private DropOSProtocol protocol;

	public CoordinatorConnectionHandler(BlockingQueue<Socket> queue) {
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
			respondWithIndex((FileAndMessage) msg);
			break;
		}
		
		
	}

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
		
		System.out.println("[Server] These were the following changes received:\n" + resolution);	
	}

	// TODO This is supposed to check the server-side resolution if a file is indeed valid. If so, it should return true to accept the file.
	private boolean isValid(PacketHeader headers) {
		
		return false;
	}
}