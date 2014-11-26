package dropos;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientConnection extends Thread {
	private DataInputStream in;
	public PrintWriter out;
	private String ipAddress;

	public ClientConnection(Socket connectionSocket) {
		this.ipAddress = connectionSocket.getInetAddress().toString()
				.substring(1);
		
		System.out.println("Server has accepted connection from client [" + ipAddress + "]");
		
		try {
			in = new DataInputStream(connectionSocket.getInputStream());
			// InputStreamReader bin = new InputStreamReader(ins);
			// in = new BufferedReader(bin);

			out = new PrintWriter(connectionSocket.getOutputStream(), true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String input;
		// As long as you are sending me messages, handle them correctly
		try {
			while((input = in.readUTF()) != null){
				System.err.println("Client: " + ipAddress);
				handleInput(input);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void handleInput(String input) {
		String command = input.split(" ")[0];
		String params = input.substring(command.length());

		System.out.println("Command: " + command);
		System.out.println("Params: " + params);
		switch (command) {
		case "ADD":
			break;
		case "MODIFY":
			break;
		case "DELETE":
			break;
		}
	}

	public String getIPAddress() {
		return ipAddress;
	}
}
