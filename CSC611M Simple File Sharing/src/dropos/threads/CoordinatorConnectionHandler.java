package dropos.threads;

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
import message.packet.DuplicatePacketHeader;
import message.packet.PacketHeader;
import dropos.Config;
import dropos.DropCoordinator;
import dropos.DropServer;
import dropos.Host;
import dropos.Host.HostType;

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
	
	
	private static ArrayList<Host> connectedServers, connectedClients;
	private static Redundancy redundancyList;

	public CoordinatorConnectionHandler(BlockingQueue<Socket> queue) {
		this.queue = queue;
		this.start();

		connectedClients = new ArrayList<Host>();
		connectedServers = new ArrayList<Host>();
		redundancyList = new Redundancy();
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.connectionSocket = queue.take();
				host = new Host(connectionSocket);
				
				protocol = host.createProtocol(connectionSocket);
				log("Accepting headers on a new DropOSProtocol with host [" + host + "]");

				PacketHeader headers = protocol.receiveHeader();
				Message msg = headers.interpret(protocol);
				log("Message from host " + host + "");
				log(headers.toString());
				System.out.println();
				
				interpretMessage(msg);
				System.out.println();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	private void interpretMessage(Message msg) throws UnknownHostException, IOException {
		String command = msg.message.split(":")[0];
		command = command.toUpperCase();
		
		FileAndMessage fileAndMsg = null;
		if (msg instanceof FileAndMessage)
			fileAndMsg = (FileAndMessage)msg;
		
		switch(command){
		case "SREGISTER":
			addServer(host, msg);
			break;
			
		case "CREGISTER":
			addClient(host, msg);
			
			break;
		
		case "INDEX":
			respondWithIndex(fileAndMsg);
			break;
		
		case "UPDATE":
			handleUpdate(fileAndMsg);
			break;
		
		case "REQUEST":
			respondWithFile(fileAndMsg);
			break;
		
		case "DELETE":
			// Check for server redundancies from the File and Server Redundancies list before doing resolution
			break;
		}
		
	}


	private void addServer(Host host, Message msg) {
		if (connectedServers.contains(host)){
			log("Host " + host + " already registered. Ignoring registration message.");
			return;
		}
		connectedServers.add(host);
		host.setType(HostType.Server);
		String port = msg.message.replace("SREGISTER:", "");
		host.setPort(Integer.parseInt(port));
		log("Registered host [" + host + "] as a server connection.");
	}

	private void addClient(Host host, Message msg) {
		if (connectedServers.contains(host)){
			log("Host " + host + " already registered. Ignoring registration message.");
			return;
		}
		connectedClients.add(host);
		host.setType(HostType.Client);
		String port = msg.message.replace("CREGISTER:", "");
		host.setPort(Integer.parseInt(port));
		log("Registered host [" + host + "] as a client connection.");
	}

	private void handleUpdate(FileAndMessage msg) {
		try {
			String filePath = msg.getFile().toPath().toString();
			String filename = msg.getFile().getName();
			
			int numberOfServers = connectedServers.size();
			
			// this is the number of servers required for duplication
			int onethirdReliability = Math.round((numberOfServers * 1 / 3) + 1);
			
			ArrayList<Host> selectedServersForRedundancy;
			
			// If it already exists in the redundancy list, then use it
			if (redundancyList.containsKey(filename)){
				selectedServersForRedundancy = redundancyList.get(filename);
				
			// If it doesn't, then (1) determine how many servers needed and (2) add them mark them for redundancy
			}else {
				Random rand = new Random();
				int sRand;
				
				// create a new bucket of hosts
				selectedServersForRedundancy = new ArrayList<Host>();
				redundancyList.put(filename, selectedServersForRedundancy);
				
				// determine which hosts go into this bucket
				for (int r = 0; r < onethirdReliability; r++) {
					do {
						sRand = rand.nextInt(numberOfServers);
					} while (selectedServersForRedundancy.contains(connectedServers.get(sRand)));
					
					selectedServersForRedundancy.add(connectedServers.get(sRand));
				}
	
			}
						
			// create the duplicate packet header
			DuplicatePacketHeader update = PacketHeader.createDuplicate(filePath, Config.getPort(), selectedServersForRedundancy);
			
			// NOTE: duplicate packet header has method that parses the ip's and creates an updatepacket header :/
			
			Host arbitraryFirstHost = selectedServersForRedundancy.get(0);
			
			protocol = arbitraryFirstHost.createProtocol();
			
			// send the file to the redundancies
			protocol.sendFile(update, msg.getFile());
			
		}catch(Exception e){
			log(e.getMessage());
		}
	}

	private void respondWithFile(FileAndMessage msg) {
		String filename = msg.getFile().getName();
		
		if (redundancyList.containsKey(filename) == false){
			log("Coordinator could not find file " + filename + ". Most likely it doesn't have this file yet.");
			log("Coordinator exits and will not respond to request.");
			return;
		}
		
		// Find out who has the file
		ArrayList<Host> servers = redundancyList.get(filename);
		
		if (servers.size() <= 0){
			log("There are no servers which have this file. This is a weird bug.");
			return;
		}
		
		// Choose a random server
		Host host = servers.get(0);
		
		// Get the file
		DropOSProtocol createProtocol = host.createProtocol();
		PacketHeader packetHeader = PacketHeader.createRequest(filename, Config.getPort());
		createProtocol.sendMessage(packetHeader);
		
		// TODO: Unfinished code
		
		// Pass back to client 
	}

	private void respondWithIndex(FileAndMessage msg) throws UnknownHostException, IOException {
		
		log("A new socket connection is being made...");
		protocol = host.createProtocol();
		
		log("Sending the server's index list.");
		// Respond by sending your own index
		protocol.sendIndex(Config.getPort());
		
	}

	private static void log(String message){
		System.out.println("[Coordinator] " + message);
	}
	
	private class Redundancy extends HashMap<String, ArrayList<Host>>{
		/**
		 * This method is used to mark which servers have a redundancy of this file.
		 * @param filename the file
		 * @param servers the servers which have the file
		 */
		public ArrayList<Host> put(String filename, ArrayList<Host> servers) {
			// TODO Auto-generated method stub
			return super.put(filename, servers);
		}
	}

}
