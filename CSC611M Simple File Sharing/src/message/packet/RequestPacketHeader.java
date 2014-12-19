package message.packet;

import java.io.File;
import java.io.IOException;

import message.DropOSProtocol;
import message.FileAndMessage;
import message.Message;
import dropos.Config;

public class RequestPacketHeader extends FilePacketHeader {
	
	public RequestPacketHeader(int port, String filename) {
		super(port, "REQUEST:" + filename);
		this.filename = filename;
	}

	@Override
	public Message interpret(DropOSProtocol protocol) throws IOException {
		File f = new File(Config.getInstancePath(port) + "\\" +  filename);
		return new FileAndMessage(header, f);
	}
	
	protected File receiveFile(DropOSProtocol protocol) throws IOException {
		return protocol.receiveFile(filePath(), filesize);
	}
	
	protected String filePath() {
		return Config.getInstancePath(port) + "\\" + filename;
	}
}
