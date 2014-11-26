package dropos;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
		System.out.println("Running client connection thread");
		String input;
		// As long as you are sending me messages, handle them correctly
//		try {
//			while((input = in.readUTF()) != null){
//				System.err.println("Client: " + ipAddress);
//				handleInput(input);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		
		try {			
			File f = new File("./ServerFile/test.txt");
			FileOutputStream fos = new FileOutputStream(f);
			
			long filesize = 12312314;
			System.out.println("File size of " + filesize);
			byte [] mybytearray = new byte [(int)filesize];
			int bytesRead;
			int currentTot = 0;
			
			bytesRead = in.read(mybytearray, 0, mybytearray.length);
			currentTot = bytesRead;
			
			do {
				bytesRead = in.read(mybytearray, currentTot, mybytearray.length-currentTot);
				if (bytesRead >= 0) currentTot += bytesRead;
			}while(bytesRead > -1);
			
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(mybytearray, 0, currentTot);
			
			bos.flush();
			bos.close();
			fos.close();
//			
//			System.out.println("File size: " + filesize);
//			
//			int n = 0;
//			while (filesize > 0 && (n = in.read(mybytearray, 0,(int) Math.min(filesize, mybytearray.length))) != -1) {
//				fos.write(mybytearray, 0, n);
//				fos.flush();
//				filesize -= n;
//
//			}
			System.out.println("DONE DOWNLOAD");
			//fos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
