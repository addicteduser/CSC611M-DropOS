package message;

import java.io.IOException;

public class ServerIndexListPacketHeader extends IndexListPacketHeader {
	String ipAddress;
	
	public ServerIndexListPacketHeader(String header) {
		super(header);
	}
	
	protected String filePath() {
		return ipAddress + ".txt";
	}
	
	@Override
	public Message interpret(DropOSProtocol protocol) throws IOException {
		ipAddress = protocol.getIPAddress();
		return super.interpret(protocol);
	}
}
