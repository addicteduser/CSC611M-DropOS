package message;

import java.io.UnsupportedEncodingException;

import dropos.event.SynchronizationEvent;

public abstract class PacketHeader {
	String header;
	public PacketHeader(String header) {
		this.header = header;
	}
	
	@Override
	public String toString() {
		return header;
	}

	public static PacketHeader create(SynchronizationEvent event) {
		return create(event.toString());
	}
	
	public static PacketHeader create(String header) {
		PacketHeader result = null;
		String command = header.split(" ")[0].toUpperCase().trim();
		
		switch(command){
		case "INDEX":
			result = new IndexListPacketHeader(header);
			break;
		case "REQUEST":
			break;
		case "UPDATE":
			break;
		case "DELETE":
			break;
		}
		return result;
	}

	public byte[] getBytes() throws UnsupportedEncodingException {
		return header.getBytes("UTF-8");
	}

	
}
