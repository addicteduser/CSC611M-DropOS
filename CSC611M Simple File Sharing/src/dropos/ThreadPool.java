package dropos;

import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {
	private final BlockingQueue<Socket> socketQueue;
	private ArrayList<ClientConnection> threadList = new ArrayList<ClientConnection>();
	
	public ThreadPool(int numThread){
		//Queue for storing "work" (In this case sockets)
		this.socketQueue = new LinkedBlockingQueue();
			
		//Create threads for pool
		for(int i = 0; i<numThread;i++){
			threadList.add(new ClientConnection(socketQueue));
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
