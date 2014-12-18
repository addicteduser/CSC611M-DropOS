package message;

public class DuplicatePacketHeader extends PacketHeader{

	public DuplicatePacketHeader(String header) {
		super(header);
		String[] split = header.split(":");
		// get the ip addresses
		// DUPLICATE:ipaddress1:ipaddress2:ipaddressN
	}

	public Message interpret(DropOSProtocol protocol) {
		
		return new Message(header);
	}
}
