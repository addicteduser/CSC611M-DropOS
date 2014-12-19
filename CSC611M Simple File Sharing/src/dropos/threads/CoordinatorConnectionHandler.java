package dropos.threads;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
	private static ArrayList<FileAndServerPairs> redundanciesList;

	public CoordinatorConnectionHandler(BlockingQueue<Socket> queue) {
		this.queue = queue;
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
				host = new Host(connectionSocket);
				log("Acquired lock on host " + host + ".");
				
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
			
			int numberOfServers = connectedServers.size();
			
			// this is the number of servers required for duplication
			int onethirdReliability = Math.round((numberOfServers * 1 / 3) + 1);
			
			ArrayList<Host> selectedServersForRedundancy = new ArrayList<Host>();
			Random rand = new Random();
			int sRand;
			
			// select the servers
			for (int r = 0; r < onethirdReliability; r++) {
				do {
					sRand = rand.nextInt(numberOfServers);
				} while (selectedServersForRedundancy.contains(connectedServers.get(sRand)));
				
				selectedServersForRedundancy.add(connectedServers.get(sRand));
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
		// Find out who has the file
		
		// Choose a random server
		
		// Get the file 
		
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
	
	private class FileAndServerPairs {
		String fileName;
		ArrayList<Host> servers;
		
		public FileAndServerPairs(String filename, ArrayList<Host> servers) {
			this.fileName = filename;
			this.servers = servers;
		}
	}

}
