package message;

import java.io.IOException;

public class RegisterPacketHeader extends PacketHeader {

	public RegisterPacketHeader(String header) {
		super(header);
	}
	
	
	public Message interpret(DropOSProtocol protocol) throws IOException {
		return new Message(header);
	}

}
