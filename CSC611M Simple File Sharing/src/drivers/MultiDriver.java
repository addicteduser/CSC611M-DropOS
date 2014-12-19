package drivers;

import dropos.Config;
import dropos.DropClient;
import dropos.DropCoordinator;
import dropos.DropServer;

public class MultiDriver {

	public static void main(String[] args) {
		Config.initialize();
		Thread thread;
		
		// Create one coordinator, do this first
		System.out.println("Generating coordinator...");
		DropCoordinator coordinator = DropCoordinator.create();
		thread = new Thread(coordinator);
		thread.start();
		System.out.println("Coordinator generated.\n");
		
		try {
		
		// Create the servers
		int servers = 3;
		System.out.println("Generating " + servers + " server/s...");
		for (int i = 0; i < servers; i++){
			DropServer s = DropServer.create();
			thread = new Thread(s);
			thread.start();	
			System.out.println("Server " + (i + 1) + " generated.\n");
			Thread.sleep(1000);
		}
		
		// Create the clients
		int clients = 2;
		System.out.println("Generating " + clients + " client/s...");
		for (int i = 0; i < clients; i++){
			DropClient c = DropClient.create();
			thread = new Thread(c);
			thread.start();
			System.out.println("Client " + (i + 1) + " generated.\n");
			Thread.sleep(1000);
		}
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
