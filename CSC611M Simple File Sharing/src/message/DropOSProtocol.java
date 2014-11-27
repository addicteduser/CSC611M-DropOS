package message;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import dropos.Config;

public class DropOSProtocol {
	// Reading
	private BufferedReader bufferedReader;
	private InputStream inputStream;
	
	// Writing
	private PrintWriter printWriter;
	private String ipAddress;

	public DropOSProtocol(Socket s) {
		try {
			inputStream = s.getInputStream();
			InputStreamReader isr = new InputStreamReader(inputStream);
			
			bufferedReader = new BufferedReader(isr);
			printWriter = new PrintWriter(s.getOutputStream(), true);
			ipAddress = s.getInetAddress().toString().substring(1);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) {
		printWriter.println(message);
	}

	public void sendFile(File file) {
		
	}

	public File receiveFile(String filePath) {
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		File file = null;
		
		try {
		file = new File(Config.getPath() + "\\" + filePath);
		
		// Stream to handle file writing
		fileOutputStream = new FileOutputStream(file);
		
		// Buffered output stream for file writing
		bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		
		long filesize = 100000;
		byte[] mybytearray = new byte[(int) filesize];
		int bytesRead = 0;
		int currentTot = 0;
		
		do {
			bytesRead = inputStream.read(mybytearray, currentTot, mybytearray.length - currentTot);
			if (bytesRead >= 0)
				currentTot += bytesRead;
		} while (bytesRead > -1);
		
		// Write everything
		bufferedOutputStream.write(mybytearray, 0, currentTot);
		
		// Close it
		bufferedReader.close();
		fileOutputStream.close();
		bufferedOutputStream.flush();
		
		}catch(FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();	
		}
		return file;
	}

	public String receiveMessage() throws IOException {
		return bufferedReader.readLine();
	}

	public String getIPAddress() {
		return ipAddress;
	}
}
