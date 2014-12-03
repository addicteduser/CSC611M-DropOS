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
	private String ipAddress;
	private Socket socket;

	private BufferedInputStream bufferedInputStream;
	private BufferedOutputStream bufferedOutputStream;
	private int headerBytesRead;
	
	/**
	 * The packet header has a length of fifty (50) bytes.
	 */
	private static final int PACKET_HEADER_LENGTH = 50;

	public DropOSProtocol(Socket s) {
		this.socket = s;
		try {
			InputStream inputStream = s.getInputStream();
			bufferedInputStream = new BufferedInputStream(inputStream);

			OutputStream outputStream = s.getOutputStream();
			bufferedOutputStream = new BufferedOutputStream(outputStream);

			ipAddress = s.getInetAddress().toString().substring(1);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendHeader(String message) {
		try {
			byte[] buf = new byte[PACKET_HEADER_LENGTH];
			byte[] mes = message.getBytes("UTF-8");
			buf[0] = (byte)mes.length;
			System.arraycopy(mes, 0, buf, 1, mes.length);
			
			bufferedOutputStream.write(buf, 0, buf.length);
			bufferedOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendFile(File file) {
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] buf = new byte[(int) file.length()];

			BufferedInputStream bin = new BufferedInputStream(fileInputStream);
			bin.read(buf, 0, buf.length);
			bufferedOutputStream.write(buf, 0, buf.length);
			bufferedOutputStream.flush();
			fileInputStream.close();

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
			int currentTot = headerBytesRead;

			do {
				bytesRead = bufferedInputStream.read(mybytearray, currentTot, mybytearray.length - currentTot);
				if (bytesRead >= 0)
					currentTot += bytesRead;
			} while (currentTot < filesize);

			// Write everything to file
			bufferedOutputStream.write(mybytearray, 0, currentTot);

			// Close it
			fileOutputStream.close();
			bufferedOutputStream.flush();
			socket.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	public String receiveHeader() throws IOException {
		String message = null;
		try {

			byte[] size = new byte[1];
			bufferedInputStream.read(size, 0, 1);
			
			int length = size[0];
			
			byte[] buf = new byte[(int) length];
			int bytesRead = 0;
			headerBytesRead = 0;
			
			

			do {
				bytesRead = bufferedInputStream.read(buf, headerBytesRead, length - headerBytesRead);
				if (bytesRead >= 0)
					headerBytesRead += bytesRead;
			} while (headerBytesRead < length);

			message = new String(buf);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;
	}

	public String getIPAddress() {
		return ipAddress;
	}
}
