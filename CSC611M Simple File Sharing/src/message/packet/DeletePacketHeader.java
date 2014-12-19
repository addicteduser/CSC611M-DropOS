package message.packet;

import java.io.File;
import java.nio.file.Files;

import message.DropOSProtocol;
import message.Message;
import dropos.Config;

public class DeletePacketHeader extends PacketHeader {
	protected String filename;
	
	public DeletePacketHeader(int port, String filename) {
		super(port, "DELETE:" + filename);
		this.filename = filename;
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
