package dropos.threads;

import indexer.Index;
import indexer.Resolution;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import message.DropOSProtocol;
import message.FileAndMessage;
import message.Message;
import message.PacketHeader;
import dropos.Config;
import dropos.DropCoordinator;
import dropos.DropServer;

/**
 * During initialization, a {@link ConnectionHandler} is made to block at the queue which holds the pending {@link Socket} instances to be handled.
 * Once a new instance is available, the {@link Socket} is received and is then handled.
 * 
 * <p><b>Note:</b> This class is meant to handle connections from a {@link DropServer} to a {@link DropCoordinator}. 
 *
 */
public class ToCoordinatorConnectionHandler extends Thread {
	private BlockingQueue<Socket> queue;
	private Socket connectionSocket;
	private DropOSProtocol protocol;
	private ArrayList<String> connectedServers;
	private ArrayList<FileAndServerRedundanciesPairs> redundanciesList;

	public ToCoordinatorConnectionHandler(BlockingQueue<Socket> queue) {
		this.queue = queue;
		this.start();
		
		connectedServers = new ArrayList<String>();
		redundanciesList = new ArrayList<FileAndServerRedundanciesPairs>();
	}
	
	private class FileAndServerRedundanciesPairs {
		String fileName;
		ArrayList<String> serverIPs;
		
		public FileAndServerRedundanciesPairs(String filename, ArrayList<String> serverips) {
			this.fileName = filename;
			this.serverIPs = serverips;
		}
	}


	@Override
	public void run() {
		while (true) {
			try {

				this.connectionSocket = queue.take();
				protocol = new DropOSProtocol(connectionSocket);

				System.out.println("[Coordinator] has accepted a connection from [" + protocol.getIPAddress() + "]");

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
		case "REGISTER":
			connectedServers.add(protocol.getIPAddress());
			System.out.println("[Coordinator] Connection from [" + protocol.getIPAddress() + "] is a SERVER connection");
			break;
		
		case "INDEX":
			respondWithIndex((FileAndMessage) msg);
			break;
		
		case "UPDATE":
			// Check for server redundancies from the File and Server Redundancies list before doing resolution
			// if file doesn't exist yet in the servers, let the coordinator choose na lang
			break;
		
		case "REQUEST":
			// Choose a server from the File and Server Redundancies list that will give the file.
			break;
		
		case "DELETE":
			// Check for server redundancies from the File and Server Redundancies list before doing resolution
			break;
		}
		
		
	}

	private void respondWithIndex(FileAndMessage msg) throws UnknownHostException, IOException {
		System.out.println("[Coordinator] A new socket connection is being made...");
		protocol = new DropOSProtocol(new Socket(protocol.getIPAddress(), Config.getPort()));
		
		System.out.println("[Coordinator] Sending the coordinator's index list.");
		// Respond by sending your own index
		protocol.sendIndex();

		System.out.println("[Coordinator] Performing resolution...");
		// Parse the client's index
		Index clientIndex = Index.read(msg.getFile());
		
		// Get your own index
		Index serverIndex = Index.getInstance();
		
		// Perform resolution afterwards
		Resolution resolution = Resolution.compare(serverIndex, clientIndex);
		
		System.out.println("[Coordinator] These were the following changes received:\n" + resolution);	
	}

	// TODO This is supposed to check the server-side resolution if a file is indeed valid. If so, it should return true to accept the file.
	private boolean isValid(PacketHeader headers) {
		
		return false;
	}
}