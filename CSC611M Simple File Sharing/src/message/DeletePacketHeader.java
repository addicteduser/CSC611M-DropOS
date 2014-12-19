package message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import dropos.Config;

public class DeletePacketHeader extends PacketHeader {
	protected String filename;
	private int port;
	public DeletePacketHeader(String header, int port) {
		super(header);
		this.port = port;
		filename = header.split(":")[1];
	}

	public Message interpret(DropOSProtocol protocol) {
		File f = null;
		try {
			f = new File(Config.getInstancePath(port) + "\\" +  filename);
			Files.delete(f.toPath());
		} catch(Exception e) {
			System.err.println("Could not delete " + f.toPath());
		}
		return new Message(header);
	}
}
