package message;

import indexer.Index;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import message.packet.IndexListPacketHeader;
import message.packet.PacketHeader;
import dropos.Config;
import dropos.Host;
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

	private DropOSProtocol() throws IOException {
		try {
			socket = new Socket(Config.getIpAddress(), Config.getPort());
			initialize(socket);
		} catch (ConnectException e) {
			loge("Fatal error. Could not connect to coordinator at " + Config.getIpAddress() + ".");
			loge("Please check config.ini if the coordinator's IP address is configured properly.");
			System.out.println();
			System.exit(1);
		}
	}

	private void loge(String message) {
		System.err.println("[Protocol] " + message);
	}

	public DropOSProtocol(Socket s) {
		initialize(s);
	}

	private void initialize(Socket s) {
		socket = s;

		port = socket.getPort();
		if (port < 4040 || port > 4050) {
			port = socket.getLocalPort();
		}

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

	public void sendMessage(PacketHeader packetHeader) {
		try {
			log("---" + packetHeader.toString()+"---------------------------->");
			byte[] buf = new byte[BUFFER_LENGTH];
			byte[] mes = packetHeader.toString().getBytes("UTF-8");
			byte[] packetHeaderLength = intToByteArray(mes.length);

			// First 4 bytes contain an integer value, which is the length of the packet header
			System.arraycopy(packetHeaderLength, 0, buf, 0, 4);

			// The next bytes would be the packet header
			System.arraycopy(mes, 0, buf, 4, mes.length);

			bufferedOutputStream.write(buf, 0, buf.length);
			bufferedOutputStream.flush();
		} catch (UnsupportedEncodingException e) {
			log("Message could not be converted into bytes.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendIndex(int port) throws IOException {
		Index index = Index.getInstance(port);
		index.write(port);
		IndexListPacketHeader packetHeader = index.getPacketHeader(port);
		File file = index.getFile();
		sendFile(packetHeader, file);
	}

	public void sendFile(PacketHeader header, File f) throws IOException {
		log("---" + header.toString()+"---------------------------->");
		// header
		byte[] buf = new byte[BUFFER_LENGTH];
		byte[] mes = header.getBytes();
		byte[] packetHeaderLength = intToByteArray(mes.length);

		// First 4 bytes contain an integer value, which is the length of the packet header
		// System.arraycopy(packetHeaderLength, 0, buf, 0, 4);
		bufferedOutputStream.write(packetHeaderLength, 0, packetHeaderLength.length);

		// The next bytes would be the packet header
		// System.arraycopy(mes, 0, buf, 4, mes.length);
		bufferedOutputStream.write(mes, 0, mes.length);

		// The last stream of bytes contains the payload; the file
		FileInputStream fileInputStream = new FileInputStream(f);
		BufferedInputStream bin = new BufferedInputStream(fileInputStream);

		int fileBytesWritten = 0;
		do {
			int bytesRead = bin.read(buf, fileBytesWritten, BUFFER_LENGTH);
			// If you read something enough to fit the buffer, then write it out to the socket
			if (bytesRead > 0) {
				bufferedOutputStream.write(buf, fileBytesWritten, bytesRead);
				fileBytesWritten += bytesRead;
			}
		} while (fileBytesWritten < f.length());

		fileInputStream.close();
		bufferedOutputStream.flush();
		bufferedOutputStream.close();
	}

	private void log(String message) {
		System.out.println("[Protocol] " + message);
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
	public void performSynchronization(SynchronizationEvent event, File f, int port) throws IOException {
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
		System.out.println();
		System.out.println("Receiving file...");
		file = new File(filePath);
		
		// If the destination folders aren't created yet, create them.
		Path parent = file.toPath().getParent();
		if (Files.notExists(parent)){
			Files.createDirectories(parent);
		}
		
		if (Files.notExists(file.toPath()))
		{
			Files.createFile(file.toPath());
		}

		// Stream to handle file writing
		fileOutputStream = new FileOutputStream(file);

		// Buffered output stream for file writing
		bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

		byte[] buf = new byte[BUFFER_LENGTH];
		int bytesRead = 0;
		int totalBytesRead = 0;

		do {
			// Read from input stream into buffer
			bytesRead = bufferedInputStream.read(buf, totalBytesRead, BUFFER_LENGTH);

			// If you read something, then write it out onto the file
			if (bytesRead >= 0) {
				// Write buffer into to file
				bufferedOutputStream.write(buf, totalBytesRead, bytesRead);

				totalBytesRead += bytesRead;
			}
		} while (totalBytesRead < filesize);

		// Close it
		bufferedOutputStream.flush();
		bufferedOutputStream.close();
		
		if (socket.isClosed() == false)
			socket.close();
		System.out.println("Received.\n");
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
		
		message = new String(buf);
		
		if (bufferedInputStream.available() == 0)
			socket.close();
		
		log("<--------------------------"+ message +"-----");
		return PacketHeader.parsePacket(message, port);
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
