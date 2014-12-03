package dropos;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import message.DropOSProtocol;

public class DropServer extends Thread {
	private BlockingQueue<Socket> queue;
	private Socket connectionSocket;
	private DropOSProtocol protocol;

	public DropServer(BlockingQueue<Socket> queue) {
		this.queue = queue;
		this.start();
	}

	@Override
	public void run() {
		while (true) {
			try {

				this.connectionSocket = queue.take();
				protocol = new DropOSProtocol(connectionSocket);

				System.out
						.println("Server has accepted connection from client ["
								+ protocol.getIPAddress() + "]");

				String headers = protocol.receiveHeader();
				handleInput(headers);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void handleInput(String input) {
		String command = input.split(" ")[0];
		String params = input.substring(command.length() + 1).trim();

		switch (command) {
		case "ADD":
			String fileSize = params.split(" ")[0];
			String fileName = params.substring(fileSize.length() + 1).trim();
			long size = Long.valueOf(fileSize);
			
			protocol.receiveFile(fileName, size);
			break;
		case "MODIFY":
			break;
		case "DELETE":
			break;
		}

	}

}
