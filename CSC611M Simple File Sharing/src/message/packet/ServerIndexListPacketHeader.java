package message.packet;

import java.io.IOException;

import message.DropOSProtocol;
import message.Message;

public class ServerIndexListPacketHeader extends IndexListPacketHeader {
	String ipAddress;
	
	public ServerIndexListPacketHeader(int port, String header) {
		super(port, header);
	}
	
	@Override
	public Message interpret(DropOSProtocol protocol) throws IOException {
		ipAddress = protocol.getIPAddress();
		return super.interpret(protocol);
	}
}
