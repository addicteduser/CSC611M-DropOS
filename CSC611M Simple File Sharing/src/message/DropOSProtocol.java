package message;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
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
	 * The packet header has a length of fifty (50) bytes.
	 */
	private static final int PACKET_MAX_LENGTH = 1500;

	public DropOSProtocol(Socket s) {
		this.socket = s;
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

	public void sendHeader(String message) throws IOException {
		byte[] buf = new byte[PACKET_MAX_LENGTH];
		byte[] mes = message.getBytes("UTF-8");
		buf[0] = (byte) mes.length;
		System.arraycopy(mes, 0, buf, 1, mes.length);

		bufferedOutputStream.write(buf, 0, buf.length);
		bufferedOutputStream.flush();
	}

	public void sendFile(File file) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buf = new byte[(int) file.length()];

		BufferedInputStream bin = new BufferedInputStream(fileInputStream);
		bin.read(buf, 0, buf.length);
		bufferedOutputStream.write(buf, 0, buf.length);
		bufferedOutputStream.flush();
		fileInputStream.close();

	}

	public void sendHeaderAndFile(SynchronizationEvent event, File f) throws IOException {
		long size;
		size = Files.size(f.toPath());
		String message = ("ADD " + size + " " + event.getFile());

		// header
		byte[] buf = new byte[PACKET_MAX_LENGTH];
		byte[] mes = message.getBytes("UTF-8");
		buf[0] = (byte) mes.length;
		System.arraycopy(mes, 0, buf, 1, mes.length);

		// file
		FileInputStream fileInputStream = new FileInputStream(f);
		byte[] fbuf = new byte[(int) f.length()];

		BufferedInputStream bin = new BufferedInputStream(fileInputStream);
		bin.read(fbuf, 0, fbuf.length);

		System.arraycopy(fbuf, 0, buf, mes.length + 1, fbuf.length);
		bufferedOutputStream.write(buf, 0, buf.length);
		bufferedOutputStream.flush();
		fileInputStream.close();
	}

	public File receiveFile(String filePath, long filesize) throws IOException {
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		File file = null;

		file = new File(Config.getPath() + "\\" + filePath);

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

	public String receiveHeader() throws IOException {
		String message = null;

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

		return message;
	}

	public String getIPAddress() {
		return ipAddress;
	}
}
