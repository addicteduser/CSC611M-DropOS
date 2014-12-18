package dropos.threads;

import indexer.Index;
import indexer.Resolution;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
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
 * <p><b>Note:</b> This class is meant to handle connections from a {@link DropCoordinator} to a {@link DropServer}. 
 *
 */
public class CoordinatorConnectionHandler extends Thread {
	private BlockingQueue<Socket> queue;
	private Socket connectionSocket;
	private DropOSProtocol protocol;
	private ArrayList<String> connectedServers;
	private ArrayList<FileAndServerRedundanciesPairs> redundanciesList;
	private HashMap<String, Resolution> resolutions;

	public CoordinatorConnectionHandler(BlockingQueue<Socket> queue) {
		this.queue = queue;
		resolutions = new HashMap<String, Resolution>();
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
		case "REGISTER":
			connectedServers.add(protocol.getIPAddress());
			System.out.println("[Coordinator] Connection from [" + protocol.getIPAddress() + "] is a SERVER connection");
			break;
		
		case "INDEX":
			respondWithIndex((FileAndMessage) msg);
			break;
		
		case "UPDATE":
			verifyUpdate((FileAndMessage)msg);
			break;
		
		case "REQUEST":
			respondWithRequest((FileAndMessage)msg);
			// Choose a server from the File and Server Redundancies list that will give the file.
			break;
		
		case "DELETE":
			// Check for server redundancies from the File and Server Redundancies list before doing resolution
			break;
		}
		
	}


	private void verifyUpdate(FileAndMessage msg) {
		try {
			String ipAddress = protocol.getIPAddress();
			String filename = msg.getFile().toString();
			
			if (resolutions.containsKey(ipAddress) == false)
				throw new Exception("Invalid UPDATE message received. Client did not send me his index file.");
			
			Resolution resolution = resolutions.get(ipAddress);
			String action = resolution.get(filename);
			
			if (action.equalsIgnoreCase("UPDATE") == false)
				throw new Exception("Invalid UPDATE message received. File is not marked for update.");
			
			int numberOfServers = connectedServers.size();
			int onethirdReliability = Math.round((numberOfServers * 1 / 3) + 1);
			
			ArrayList<String> redList = new ArrayList<String>();
			Random rand = new Random();
			int sRand;
			
			for (int r = 0; r < onethirdReliability; r++) {
				do {
					sRand = rand.nextInt(numberOfServers);
				} while (redList.contains(connectedServers.get(sRand)));
				
				redList.add(connectedServers.get(sRand));
			}
			
			String duplicateHeader = "DUPLICATE";
			
			for(String ipAdd : redList) {
				duplicateHeader += ":" + ipAdd;
			}
			
			duplicateHeader += "\n";
			
			File f = new File(filename);
			String updateHeader = "UPDATE:" + f.length() + ":" + f.getName();
			
			String header = duplicateHeader + updateHeader;
			
			PacketHeader update = PacketHeader.create(header);
			
			Socket socket = new Socket(redList.get(0), Config.getPort());
			protocol = new DropOSProtocol(socket);
			
			protocol.sendFile(update, f);
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	private void respondWithRequest(FileAndMessage msg) {
		
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
		
		setResolution(protocol.getIPAddress(), resolution);
		
		System.out.println("[Server] These were the following changes received:\n" + resolution);	
	}
	
	

	private synchronized void setResolution(String ipAddress, Resolution resolution) {
		resolutions.put(ipAddress, resolution);
	}

	// TODO This is supposed to check the server-side resolution if a file is indeed valid. If so, it should return true to accept the file.
	private boolean isValid(PacketHeader headers) {
		
		return false;
	}
}