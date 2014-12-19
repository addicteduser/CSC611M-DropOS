package message;

import java.io.IOException;

public class RegisterPacketHeader extends PacketHeader {

	public RegisterPacketHeader(int port, String header) {
		super(port, header);
	}
	
	
	public Message interpret(DropOSProtocol protocol) throws IOException {
		return new Message(header);
	}

}
