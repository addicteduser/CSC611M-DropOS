package message;

import java.io.File;
import java.io.IOException;

import dropos.Config;

public class RequestPacketHeader extends FilePacketHeader {
	protected String filename;
	private int port;
	
	public RequestPacketHeader(String header, int port) {
		super(header);
		this.port = port;
		filename = header.split(":")[1];
	}

	@Override
	public Message interpret(DropOSProtocol protocol) throws IOException {
		File f = new File(Config.getInstancePath(port) + "\\" +  filename);
		return new FileAndMessage("REQUEST:" + filename, f);
	}
	
	protected File receiveFile(DropOSProtocol protocol) throws IOException {
		return protocol.receiveFile(filePath(), filesize);
	}
	
	protected String filePath() {
		return Config.getInstancePath(port) + "\\" + filename;
	}
}
