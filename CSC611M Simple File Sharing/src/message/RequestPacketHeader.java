package message;

import java.io.File;
import java.io.IOException;

public class RequestPacketHeader extends PacketHeader{

	protected String filename;
	
	public RequestPacketHeader(String header) {
		super(header);
		String[] split = header.split(" ");
		filename = split[1];
	}

	@Override
	public Message interpret(DropOSProtocol protocol) throws IOException {
		File file = new File(filename);
		System.out.println("The client is requesting for file: " + file);
		return new FileAndMessage("REQUEST " + filename, file);
	}

}
