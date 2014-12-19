package message;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import message.DropOSProtocol.HostType;
import dropos.event.SynchronizationEvent;

public class PacketHeader {
	String header;
	protected int port;
	public PacketHeader(int port, String header) {
		this.port = port;
		this.header = header;
	}
	
	@Override
	public String toString() {
		return header;
	}

	public static PacketHeader create(SynchronizationEvent event, int port) {
		return create(event.toString(), port);
	}
	
	/**
	 * 
	 * @param header
	 * @param port Because we are allowing multiple clients or servers to run on one computer, they must have their own folders. This requires port to be passed.
	 * @return
	 */
	public static PacketHeader create(String header, int port) {
		PacketHeader result = null;
		String command = header.split(":")[0].toUpperCase().trim();
		
		switch(command){
		case "INDEX":
			if (DropOSProtocol.type == HostType.Server)
				result = new ServerIndexListPacketHeader(port, header);
			else
				result = new IndexListPacketHeader(port, header);
			break;
		case "REQUEST":
			result = new RequestPacketHeader(port, header);
			break;
		case "UPDATE":
				result = new FilePacketHeader(port, header);
			break;
		case "DELETE":
			result = new DeletePacketHeader(port, header);
			break;
		case "SREGISTER":
		case "CREGISTER":
			result = new RegisterPacketHeader(port, header);
			break;
		}
		return result;
	}

	public byte[] getBytes() throws UnsupportedEncodingException {
		return header.getBytes("UTF-8");
	}
	
	public Message interpret(DropOSProtocol protocol) throws IOException{
		return new Message(header);
	}

	
}
