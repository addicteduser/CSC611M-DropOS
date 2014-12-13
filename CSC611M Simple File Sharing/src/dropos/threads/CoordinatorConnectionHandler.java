package dropos.threads;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import message.DropOSProtocol;
import message.FilePacketHeader;
import message.PacketHeader;
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

	public CoordinatorConnectionHandler(BlockingQueue<Socket> queue) {
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
		if (headers instanceof FilePacketHeader)
			((FilePacketHeader) headers).receiveFile(protocol);
	}
}
