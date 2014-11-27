package dropos;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import message.DropOSProtocol;

public class ClientConnection extends Thread {
	private BlockingQueue<Socket> queue;
	private Socket connectionSocket;
	private DropOSProtocol protocol;

	public ClientConnection(BlockingQueue<Socket> queue) {
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

				String headers = protocol.receiveMessage();
				protocol.sendMessage("GO");
				handleInput(headers);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void handleInput(String input) {
		String command = input.split(" ")[0];
		String params = input.substring(command.length() + 1).trim();

		System.out.println("Command: " + command);
		System.out.println("Params: " + params);
		switch (command) {
		case "ADD":
			protocol.receiveFile(params);
			break;
		case "MODIFY":
			break;
		case "DELETE":
			break;
		}

	}

}
