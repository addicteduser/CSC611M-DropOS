package message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import dropos.Config;

public class DeletePacketHeader extends PacketHeader {
	protected String filename;
	public DeletePacketHeader(String header) {
		super(header);
		filename = header.split(":")[1];
	}

	public Message interpret(DropOSProtocol protocol) {
		File f = null;
		try {
			f = new File(Config.getPath() + "\\" +  filename);
			Files.delete(f.toPath());
		} catch(Exception e) {
			System.err.println("Could not delete " + f.toPath());
		}
		return new Message(header);
	}
}
