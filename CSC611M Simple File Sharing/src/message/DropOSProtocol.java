package message;

import indexer.Index;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;

import dropos.Config;
import dropos.event.SynchronizationEvent;

public class DropOSProtocol {
	private String ipAddress;
	private Socket socket;

	private BufferedInputStream bufferedInputStream;
	private BufferedOutputStream bufferedOutputStream;
	private int headerBytesRead;

	/**
	 * The packet header has a length of 5mb.
	 */
	private static final int PACKET_MAX_LENGTH = 5 * 1024 * 1024;
	
	public DropOSProtocol() throws UnknownHostException, IOException{
		socket = new Socket(Config.getIpAddress(), Config.getPort());
		initialize(socket);
	}
	
	public DropOSProtocol(Socket s) {
		initialize(s);
	}
	
	private void initialize(Socket s){
		socket = s;
		try {
			InputStream inputStream = s.getInputStream();
			bufferedInputStream = new BufferedInputStream(inputStream);

			OutputStream outputStream = s.getOutputStream();
			bufferedOutputStream = new BufferedOutputStream(outputStream);

			ipAddress = s.getInetAddress().toString().substring(1);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) throws IOException {
		byte[] buf = new byte[PACKET_MAX_LENGTH];
		byte[] mes = message.getBytes("UTF-8");
		byte[] packetHeaderLength = intToByteArray(mes.length);
		
		// First 4 bytes contain an integer value, which is the length of the packet header
		System.arraycopy(packetHeaderLength, 0, buf, 0, 4);
		
		// The next bytes would be the packet header
		System.arraycopy(mes, 0, buf, 4, mes.length);

		bufferedOutputStream.write(buf, 0, buf.length);
		bufferedOutputStream.flush();
	}
	
	public void sendIndex() throws IOException{
		IndexListPacketHeader packetHeader = Index.getInstance().getPacketHeader();
		File file = Index.getInstance().getFile();
		sendFile(packetHeader, file);
	}
	
	public void sendFile(PacketHeader header, File f) throws IOException{
		
		// header
		byte[] buf = new byte[PACKET_MAX_LENGTH];
		byte[] mes = header.getBytes();
		byte[] packetHeaderLength = intToByteArray(mes.length);
		
		// First 4 bytes contain an integer value, which is the length of the packet header
		System.arraycopy(packetHeaderLength, 0, buf, 0, 4);
		
		// The next bytes would be the packet header
		System.arraycopy(mes, 0, buf, 4, mes.length);

		// The last stream of bytes contains the payload; the file
		FileInputStream fileInputStream = new FileInputStream(f);
		byte[] fbuf = new byte[(int) f.length()];

		BufferedInputStream bin = new BufferedInputStream(fileInputStream);
		bin.read(fbuf, 0, fbuf.length);

		System.arraycopy(fbuf, 0, buf, mes.length + 4, fbuf.length);
		bufferedOutputStream.write(buf, 0, buf.length);
		bufferedOutputStream.flush();
		fileInputStream.close();
	}

	/**
	 * This method is called when changes are detected on your directory while the program is running. 
	 * @param event the kind of event fired
	 * @param f the file that was modified/added/deleted
	 * @throws IOException
	 */
	public void performSynchronization(SynchronizationEvent event, File f) throws IOException {
		sendFile(PacketHeader.create(event), f);
	}
	
	/**
	 * This method handles receiving a file
	 * @param filePath the absolute path of where to save the file
	 * @param filesize the size of the file; in number of bytes
	 * @return the complete file received from the network
	 * @throws IOException
	 */
	public File receiveFile(String filePath, long filesize) throws IOException {
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		File file = null;

		file = new File(filePath);

		// Stream to handle file writing
		fileOutputStream = new FileOutputStream(file);

		// Buffered output stream for file writing
		bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

		byte[] mybytearray = new byte[(int) filesize];
		int bytesRead = 0;
		int currentTot = 0;

		do {
			bytesRead = bufferedInputStream.read(mybytearray, currentTot, mybytearray.length - currentTot);
			if (bytesRead >= 0)
				currentTot += bytesRead;
		} while (currentTot < filesize);

		// Write everything to file
		bufferedOutputStream.write(mybytearray, 0, currentTot);

		// Close it
		bufferedOutputStream.flush();
		bufferedOutputStream.close();

		socket.close();
		return file;
	}

	private static int byteArrayToInt(byte[] b) {
	    final ByteBuffer bb = ByteBuffer.wrap(b);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    return bb.getInt();
	}

	private static byte[] intToByteArray(int i) {
	    final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    bb.putInt(i);
	    return bb.array();
	}
	
	public PacketHeader receiveHeader() throws IOException {
		String message = null;
		int bytesRead = 0;
		
		byte[] size = new byte[4];
		
		// Keep receiving the initial byte content until you finish reading four bytes.
		int initialBytesRead = 0;
		do{
			bytesRead = bufferedInputStream.read(size, 0, 4);
			if (bytesRead >= 0)
				initialBytesRead += bytesRead;
		}while(initialBytesRead < 4);
		
		
		// Determine N; how many bytes is the packet header?
		int length = byteArrayToInt(size);

		byte[] buf = new byte[length];
		bytesRead = 0;
		headerBytesRead = 0;
		
		// Keep receiving the packet header content until you finish reading N number of bytes.
		do {
			bytesRead = bufferedInputStream.read(buf, headerBytesRead, length - headerBytesRead);
			if (bytesRead >= 0)
				headerBytesRead += bytesRead;
		} while (headerBytesRead < length);

		message = new String(buf);
		return PacketHeader.create(message);
	}

	public String getIPAddress() {
		return ipAddress;
	}
}
