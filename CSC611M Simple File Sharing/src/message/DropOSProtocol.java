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
import java.nio.file.Path;

import dropos.Config;
import dropos.event.SynchronizationEvent;

public class DropOSProtocol {
	public enum HostType {
		Client, Coordinator, Server
	}

	public static HostType type;
	private String ipAddress;
	private int port;
	private Socket socket;

	private BufferedInputStream bufferedInputStream;
	private BufferedOutputStream bufferedOutputStream;
	private int headerBytesRead;

	/**
	 * The buffer a length of 5kb.
	 */
	private static final int BUFFER_LENGTH = 5 * 1024;

	private DropOSProtocol() throws UnknownHostException, IOException {
		socket = new Socket(Config.getIpAddress(), Config.getPort());
		initialize(socket);
	}

	public DropOSProtocol(Socket s) {
		initialize(s);
	}

	private void initialize(Socket s) {
		socket = s;
		port = socket.getLocalPort();
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
		byte[] buf = new byte[BUFFER_LENGTH];
		byte[] mes = message.getBytes("UTF-8");
		byte[] packetHeaderLength = intToByteArray(mes.length);

		System.out.println("Sending message: " + message);

		// First 4 bytes contain an integer value, which is the length of the packet header
		System.arraycopy(packetHeaderLength, 0, buf, 0, 4);

		// The next bytes would be the packet header
		System.arraycopy(mes, 0, buf, 4, mes.length);

		try {
			bufferedOutputStream.write(buf, 0, buf.length);
			bufferedOutputStream.flush();
		} catch (Exception e) {
			System.out.println("Message sent!");
		}
	}

	public void sendIndex() throws IOException {
		Index index = Index.getInstance(port);
		index.write();
		IndexListPacketHeader packetHeader = index.getPacketHeader(port);
		File file = index.getFile();
		sendFile(packetHeader, file);
	}

	public void sendRequestFile(FileAndMessage msg) throws IOException {
		File f = msg.file;
		String message = "UPDATE:" + f.length() + ":" + f.getName();
		PacketHeader packetHeader = PacketHeader.create(message, port);
		sendFile(packetHeader, f);
	}

	public void sendFile(PacketHeader header, File f) throws IOException {

		// header
		byte[] buf = new byte[BUFFER_LENGTH];
		byte[] mes = header.getBytes();
		byte[] packetHeaderLength = intToByteArray(mes.length);

		try {

			int bytesWrittenOffset = 0;

			// First 4 bytes contain an integer value, which is the length of the packet header
			// System.arraycopy(packetHeaderLength, 0, buf, 0, 4);
			bufferedOutputStream.write(packetHeaderLength, bytesWrittenOffset, packetHeaderLength.length);

			bytesWrittenOffset += packetHeaderLength.length;

			// The next bytes would be the packet header
			// System.arraycopy(mes, 0, buf, 4, mes.length);
			bufferedOutputStream.write(mes, bytesWrittenOffset, mes.length);

			bytesWrittenOffset += mes.length;
			
			
			
			
			
			// The last stream of bytes contains the payload; the file
			FileInputStream fileInputStream = new FileInputStream(f);
			BufferedInputStream bin = new BufferedInputStream(fileInputStream);
			
			int fileBytesWritten = 0;
			do{
				int bytesRead = bin.read(buf, fileBytesWritten, BUFFER_LENGTH);
				// If you read something enough to fit the buffer, then write it out to the socket
				if (bytesRead > 0){
					fileBytesWritten += bytesRead;
					// System.arraycopy(fbuf, 0, buf, mes.length + 4, fbuf.length);
					bufferedOutputStream.write(buf, bytesWrittenOffset, bytesRead);
					bytesWrittenOffset += bytesRead;
				}
			}while(fileBytesWritten < f.length());

			fileInputStream.close();
			
		} catch (Exception e) {
			System.out.println("File " + f + " was sent. (Recepient closed the socket.)");
		}
	}

	/**
	 * This method is called when changes are detected on your directory while the program is running.
	 * 
	 * @param event
	 *            the kind of event fired
	 * @param f
	 *            the file that was modified/added/deleted
	 * @throws IOException
	 */
	public void performSynchronization(SynchronizationEvent event, File f) throws IOException {
		sendFile(PacketHeader.create(event, port), f);
	}

	/**
	 * This method handles receiving a file
	 * 
	 * @param filePath
	 *            the absolute path of where to save the file
	 * @param filesize
	 *            the size of the file; in number of bytes
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

		byte[] buf = new byte[BUFFER_LENGTH];
		int bytesRead = 0;
		int totalBytesRead = 0;
		int bytesWritten = 0;

		do {
			// Read from input stream into buffer
			bytesRead = bufferedInputStream.read(buf, totalBytesRead, BUFFER_LENGTH);
			
			// If you read something, then write it out onto the file
			if (bytesRead >= 0){
				// Write buffer into to file
				bufferedOutputStream.write(buf, totalBytesRead, bytesRead);
				
				totalBytesRead += bytesRead;
			}
		} while (totalBytesRead < filesize);

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

		bytesRead = bufferedInputStream.read(size, 0, 4);

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
		socket.close();
		message = new String(buf);
		return PacketHeader.create(message, port);
	}

	public String getIPAddress() {
		return ipAddress;
	}

	public boolean isFinished() {
		return socket.isClosed();
	}

	public static DropOSProtocol connectToCoordinator() {
		DropOSProtocol protocol = null;
		try {
			protocol = new DropOSProtocol();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return protocol;
	}
}
