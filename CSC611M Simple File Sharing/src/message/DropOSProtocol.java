package message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import dropos.Config;

public class DropOSProtocol {
	// Reading
	private BufferedReader bufferedReader;
	private InputStream inputStream;
	
	// Writing
	private OutputStream outputStream;
	private PrintWriter printWriter;
	
	private String ipAddress;
	private Socket socket;

	public DropOSProtocol(Socket s) {
		this.socket = s;
		try {
			inputStream = s.getInputStream();
			InputStreamReader isr = new InputStreamReader(inputStream);
			
			bufferedReader = new BufferedReader(isr);
			outputStream = s.getOutputStream();
			printWriter = new PrintWriter(outputStream, true);
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
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] mybytearray = new byte[(int) file.length()];

			BufferedInputStream bin = new BufferedInputStream(fileInputStream);
			bin.read(mybytearray, 0, mybytearray.length);
			outputStream.write(mybytearray, 0, mybytearray.length);
			outputStream.flush();
			fileInputStream.close();
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File receiveFile(String filePath, long filesize) {
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		File file = null;
		
		try {
		file = new File(Config.getPath() + "\\" + filePath);
		
		// Stream to handle file writing
		fileOutputStream = new FileOutputStream(file);
		
		// Buffered output stream for file writing
		bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		
		byte[] mybytearray = new byte[(int) filesize];
		int bytesRead = 0;
		int currentTot = 0;
		
		do {
			bytesRead = inputStream.read(mybytearray, currentTot, mybytearray.length - currentTot);
			if (bytesRead >= 0)
				currentTot += bytesRead;
		} while (currentTot < filesize);
		
		// Write everything to file
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
