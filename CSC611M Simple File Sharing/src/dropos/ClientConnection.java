package dropos;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientConnection extends Thread {

	private Socket connectionSocket;
	private Scanner in;
	private PrintWriter out;

	public ClientConnection(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
		// TODO Auto-generated constructor stub
		
		System.out.println("[SYSTEM] SERVER just connected to CLIENT with IP:" + connectionSocket.getInetAddress().toString());
		
		try {
			DataInputStream din = new DataInputStream(connectionSocket.getInputStream());
			BufferedInputStream bin = new BufferedInputStream(din);
			in = new Scanner(bin);
			
			DataOutputStream dout = new DataOutputStream(connectionSocket.getOutputStream());
			out = new PrintWriter(dout);
	
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
	}

}
