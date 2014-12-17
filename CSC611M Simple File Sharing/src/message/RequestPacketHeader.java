package message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.midi.Receiver;

import dropos.Config;

public class RequestPacketHeader extends PacketHeader {

	protected long filesize;
	protected String filename;
	protected File f;
	
	public RequestPacketHeader(String header) {
		super(header);
		String[] split = header.split(" ");
		filename = split[1];
		Path path = Config.getPath();
		f = new File(path + "\\" + filename);
		try {
			filesize = Files.size(f.toPath());
		} catch (IOException e1) {
			System.err.println("Failed to parse filesize.");
		}
		
		System.out.println("HEADER: " + header);
	}

	@Override
	public Message interpret(DropOSProtocol protocol) throws IOException {
		protocol.sendFile(this, f);
		return new FileAndMessage("REQUEST1 " + filename, f);
	}
	
	protected File receiveFile(DropOSProtocol protocol) throws IOException {
		return protocol.receiveFile(filePath(), filesize);
	}
	
	protected String filePath() {
		return Config.getPath() + "\\" + filename;
	}
	
	public Message interpret2(DropOSProtocol protocol) throws IOException {
		File file = receiveFile(protocol);
		System.out.println("Requested file received");
		return new FileAndMessage("REQUEST " + filename, file);
	}

//	public RequestPacketHeader(String header) {
//		super(header);
//		String[] split = header.split(" ");
//		
//		filename = split[1];
//		Path path = Config.getPath();
//		File f = new File(path + "\\" + filename);
//		try {
//			filesize = Files.size(f.toPath());
//		} catch (IOException e1) {
//			System.err.println("Failed to parse filesize.");
//		}
//	}
//	
//	public Message interpret(DropOSProtocol protocol) throws IOException {
//		File file = receiveFile(protocol);
//		System.out.println("Received requested file.");
//		return new FileAndMessage("REQUEST " + filename, file);
//	}

}
