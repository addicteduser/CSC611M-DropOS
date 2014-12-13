package dropos.threads;

import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import dropos.DropServer;

/**
 * The {@link CoordinatorThreadPool} creates a {@link LinkedBlockingQueue} which will hold the {@link Socket} instances added by the {@link DropServer}. 
 * One of the sixteen (16) instances of the {@link CoordinatorConnectionHandler} will then poll for the {@link Socket} and handle the connection.
 * @author Kevin
 *
 */
public class CoordinatorThreadPool {
	private final BlockingQueue<Socket> socketQueue;
	private ArrayList<CoordinatorConnectionHandler> threadList = new ArrayList<CoordinatorConnectionHandler>();
	
	public CoordinatorThreadPool(int numThread){
		//Queue for storing "work" (In this case sockets)
		this.socketQueue = new LinkedBlockingQueue();
			
		//Create threads for pool
		for(int i = 0; i<numThread;i++){
			threadList.add(new CoordinatorConnectionHandler(socketQueue));
		}

	}
	
	public void addTask(Socket s){
		//Add new "work" to queue
		try {
			socketQueue.put(s);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
	}
	
}
