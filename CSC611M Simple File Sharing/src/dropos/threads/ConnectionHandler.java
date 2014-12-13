package dropos.threads;

import indexer.Index;
import indexer.Resolution;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import message.DropOSProtocol;
import message.FilePacketHeader;
import message.IndexListPacketHeader;
import message.PacketHeader;
import dropos.DropCoordinator;
import dropos.DropServer;

/**
 * During initialization, a {@link CoordinatorConnectionHandler} is made to block at the queue which holds the pending {@link Socket} instances to be handled.
 * Once a new instance is available, the {@link Socket} is received and is then handled.
 * 
 * <p><b>Note:</b> This class is meant to handle connections from a {@link DropCoordinator} to a {@link DropServer}. 
 *
 */
public class ConnectionHandler extends Thread {
	private BlockingQueue<Socket> queue;
	private Socket connectionSocket;
	private DropOSProtocol protocol;

	public ConnectionHandler(BlockingQueue<Socket> queue) {
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
				interpretHeader(headers);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * <p>This method interprets the <b>packet headers</b> or the <b>message</b> so that the appropriate actions can be performed.</p>
	 * @param headers
	 * @throws IOException
	 */
	private void interpretHeader(PacketHeader headers) throws IOException {
		
		// If an index file is being sent, receive it no matter what.
		if (headers instanceof IndexListPacketHeader){
			File clientIndexFile = ((IndexListPacketHeader) headers).receiveFile(protocol);
			
			// Respond by sending your own index
			protocol.sendIndex();

			// Parse the client's index
			Index clientIndex = Index.read(clientIndexFile);
			
			// Get your own index
			Index serverIndex = Index.getInstance();
			
			// Perform resolution afterwards
			Resolution resolution = Resolution.compare(serverIndex, clientIndex);
			
			System.out.println("[Server] These were the following changes received:\n" + resolution);
			
		}

		// If a file is being sent, verify if it is indeed valid 
		if (headers instanceof FilePacketHeader){
			if (isValid(headers)){
				FilePacketHeader fileHeader = (FilePacketHeader) headers;
				fileHeader.receiveFile(protocol);	
			}
		}	
	}

	// TODO This is supposed to check the server-side resolution if a file is indeed valid. If so, it should return true to accept the file.
	private boolean isValid(PacketHeader headers) {
		
		return false;
	}
}
