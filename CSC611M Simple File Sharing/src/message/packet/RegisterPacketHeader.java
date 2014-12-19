package message.packet;

import java.io.IOException;

import message.DropOSProtocol;
import message.Message;

public class RegisterPacketHeader extends PacketHeader {

	public RegisterPacketHeader(int port, String registerType) {
		super(port, registerType + ":" + port);
	}
	
	
	public Message interpret(DropOSProtocol protocol) throws IOException {
		return new Message(header);
	}

}
