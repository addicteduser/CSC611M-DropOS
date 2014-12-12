package dropos;

import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link ThreadPool} creates a {@link LinkedBlockingQueue} which will hold the {@link Socket} instances added by the {@link DropServer}. 
 * One of the sixteen (16) instances of the {@link ConnectionHandler} will then poll for the {@link Socket} and handle the connection.
 * @author Kevin
 *
 */
public class ThreadPool {
	private final BlockingQueue<Socket> socketQueue;
	private ArrayList<ConnectionHandler> threadList = new ArrayList<ConnectionHandler>();
	
	public ThreadPool(int numThread){
		//Queue for storing "work" (In this case sockets)
		this.socketQueue = new LinkedBlockingQueue();
			
		//Create threads for pool
		for(int i = 0; i<numThread;i++){
			threadList.add(new ConnectionHandler(socketQueue));
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
