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
import java.util.concurrent.Semaphore;

import message.DropOSProtocol;
import message.FileAndMessage;
import message.Message;
import message.PacketHeader;
import dropos.Config;
import dropos.DropCoordinator;
import dropos.DropServer;
import dropos.Host;

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
	private Host host;
	private DropOSProtocol protocol;
	
	
	private ArrayList<Host> connectedServers, connectedClients;
	private ArrayList<FileAndServerPairs> redundanciesList;
	
	private static HashMap<Host, Resolution> resolutions;

	public CoordinatorConnectionHandler(BlockingQueue<Socket> queue) {
		this.queue = queue;
		resolutions = new HashMap<Host, Resolution>();
		this.start();

		connectedClients = new ArrayList<Host>();
		connectedServers = new ArrayList<Host>();
		redundanciesList = new ArrayList<FileAndServerPairs>();
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.connectionSocket = queue.take();
				
				host = selectHost(connectionSocket);
				log("Attempting to get lock on host " + host + ".");
				host.acquire();
				log("Acquired lock on host " + host + ".");
				

				
				protocol = host.createProtocol();
				log("Newly accepted DropOSProtocol created with host [" + host + "]");
				
				

				PacketHeader headers = protocol.receiveHeader();
				Message msg = headers.interpret(protocol);
				interpretMessage(msg);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method checks if the host is already available on the connected hosts (either client or server).
	 * @param connectionSocket
	 * @return
	 */
	private Host selectHost(Socket connectionSocket) {
		for (Host h : connectedClients){
			if (h.equals(connectionSocket)){
				return h;
			}
		}
		
		for (Host h : connectedServers){
			if (h.equals(connectionSocket)){
				return h;
			}
		}		
		return new Host(connectionSocket);
	}

	private void interpretMessage(Message msg) throws UnknownHostException, IOException {
		String command = msg.message;
		command = command.toUpperCase();
		
		switch(command){
		case "SREGISTER":
			connectedServers.add(host);
			log("Registered host [" + host + "] as a server connection.");
			break;
			
		case "CREGISTER":
			connectedClients.add(host);
			log("Registered host [" + host + "] as a client connection.");
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
			String filename = msg.getFile().toString();
			
			isValid(msg, host);
			
			int numberOfServers = connectedServers.size();
			// this is the number of servers required for duplication
			int onethirdReliability = Math.round((numberOfServers * 1 / 3) + 1);
			
			ArrayList<Host> selectedServersForRedundancy = new ArrayList<Host>();
			Random rand = new Random();
			int sRand;
			
			for (int r = 0; r < onethirdReliability; r++) {
				do {
					sRand = rand.nextInt(numberOfServers);
				} while (selectedServersForRedundancy.contains(connectedServers.get(sRand)));
				
				selectedServersForRedundancy.add(connectedServers.get(sRand));
			}
			
			String duplicateHeader = "DUPLICATE";
			
			for(Host h : selectedServersForRedundancy) {
				duplicateHeader += ":" + h.getIpAddress();
			}
			
			duplicateHeader += "\n";
			
			File f = new File(filename);
			String updateHeader = "UPDATE:" + f.length() + ":" + f.getName();
			
			String header = duplicateHeader + updateHeader;
			
			PacketHeader update = PacketHeader.create(header);
			
			Host arbitraryFirstHost = selectedServersForRedundancy.get(0);
			
			protocol = arbitraryFirstHost.createProtocol();
			protocol.sendFile(update, f);
			
		}catch(Exception e){
			log(e.getMessage());
		}
	}

	private void respondWithRequest(FileAndMessage msg) {
		
	}

	private void respondWithIndex(FileAndMessage msg) throws UnknownHostException, IOException {
		
		log("A new socket connection is being made...");
		protocol = host.createProtocol();
		
		log("Sending the server's index list.");
		// Respond by sending your own index
		protocol.sendIndex();

		log("Performing resolution...");
		// Parse the client's index
		Index clientIndex = Index.read(msg.getFile());
		
		// Get your own index
		Index serverIndex = Index.getInstance();
		
		// Perform resolution afterwards
		Resolution resolution = Resolution.compare(serverIndex, clientIndex);
		
		// Assigning resolution
		setResolution(host, resolution);
		
		log("These were the following changes received:\n" + resolution);	
	}

	private synchronized void setResolution(Host host, Resolution resolution) {
		resolutions.put(host, resolution);
	}

	private static void log(String message){
		System.out.println("[Coordinator] " + message);
	}
	
	
	// TODO This is supposed to check the server-side resolution if a file is indeed valid. If so, it should return true to accept the file.
	private boolean isValid(FileAndMessage msg, Host host) throws Exception {
			if (resolutions.containsKey(host.getIpAddress()) == false)
				throw new Exception("Invalid UPDATE message received. Client did not send me his index file.");
			
			Resolution resolution = resolutions.get(host);
			String action = resolution.get(msg.getFile().getName());
			
			if (action.equalsIgnoreCase("UPDATE") == false)
				throw new Exception("Invalid UPDATE message received. File is not marked for update.");
					
		return false;
	}
	
	private class FileAndServerPairs {
		String fileName;
		ArrayList<Host> servers;
		
		public FileAndServerPairs(String filename, ArrayList<Host> servers) {
			this.fileName = filename;
			this.servers = servers;
		}
	}

}