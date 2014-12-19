package message.packet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import message.DropOSProtocol;
import message.Message;
import message.DropOSProtocol.HostType;
import dropos.Host;
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
		File file = event.getFile().toFile();
		String filename = file.getName();
		
		switch(event.getType()){
		case DELETE:
			return createDelete(filename, port);
			
		case REQUEST:
			return createRequest(filename, port);
			
		case UPDATE:
			return createUpdate(filename, port);
		}
		return null;
	}
	
	public static IndexListPacketHeader createIndexList(String filename, int port){
		return new IndexListPacketHeader(port, filename);
	}
	
	public static IndexListPacketHeader createServerIndexList(String filename, int port){
		return new ServerIndexListPacketHeader(port, filename);
	}
	
	public static RegisterPacketHeader createServerRegister(int port){
		return createRegister("SREGISTER", port);
	}
	
	public static RegisterPacketHeader createClientRegister(int port){
		return createRegister("CREGISTER", port);
	}
	
	private static RegisterPacketHeader createRegister(String type, int port){
		return new RegisterPacketHeader(port, type);
	}
	
	public static RequestPacketHeader createRequest(String filename, int port){
		return new RequestPacketHeader(port, filename);
	}
	
	public static DeletePacketHeader createDelete(String filename, int port){
		return new DeletePacketHeader(port, filename);
	}
	
	public static UpdatePacketHeader createUpdate(String filename, int port){
		return new UpdatePacketHeader(port, filename);
	}
	
	public byte[] getBytes()  {
		try {
			return header.getBytes("UTF-8");
		}catch(UnsupportedEncodingException e){
			System.out.println("[PacketHeader] Unsupported encoding exception.");
		}
		return null;
	}
	
	public Message interpret(DropOSProtocol protocol) throws IOException{
		return new Message(header);
	}

	public static DuplicatePacketHeader createDuplicate(String filePath, int port, ArrayList<Host> redundantServers) {
		long size = new File(filePath).length();
		return new DuplicatePacketHeader(port, filePath, size, redundantServers);
	}

	public static PacketHeader parsePacket(String message, int port) {
		String command = message.split(":")[0];
		String filename = message.split(":")[1];
		
		//returns a PacketHeader of the specified type
		switch(command){
			//For request,update and delete, constructor needs port and filename
			//For index, constructor already parses the message
			//For sregister and cregister, type is specified by the command
			case "REQUEST": return new RequestPacketHeader(port,filename);
			case "UPDATE": return new UpdatePacketHeader(port,filename);
			case "SREGISTER":
			case "CREGISTER": return new RegisterPacketHeader(port,command);
			case "INDEX": return new IndexListPacketHeader(port,message);
			case "DELETE": return new DeletePacketHeader(port,filename);
			case "DUPLICATE": return new DuplicatePacketHeader(port, message);
		}
		return null;
	}

	
}
