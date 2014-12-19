package message.packet;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;

import message.DropOSProtocol;
import message.Message;
import dropos.Config;
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
		Path path = Config.getInstancePath(port).resolve(filename);
		file = path.toFile();
		
		
		
		switch(event.getType()){
		case DELETE:
			return createDelete(filename, port);
			
		case REQUEST:
			return createRequest(filename, port);
			
		case UPDATE:
			long filesize = file.length();
			return createUpdate(filename, filesize, port);
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
	
	public static UpdatePacketHeader createUpdate(String filename, long filesize, int port){
		return new UpdatePacketHeader(port, filesize, filename);
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
		String[] split = message.split(":");
		String command = split[0];
		String filename = null;
		long filesize = 0;
		
		switch(command){
		// REQUEST:FILENAME
			case "REQUEST": 
				filename = split[1];
				return new RequestPacketHeader(port,filename);
				
		// UPDATE:FILESIZE:FILENAME
			case "UPDATE":
				filesize = Long.parseLong(split[1]);
				filename = split[2];
				return new UpdatePacketHeader(port,filesize,filename);
				
		// S/CREGISTER:PORT
			case "SREGISTER":
			case "CREGISTER": 
				port = Integer.parseInt(split[1]);
				return new RegisterPacketHeader(port,command);
				
		// INDEX
			case "INDEX":
				return new IndexListPacketHeader(port,message);
				
		// DELETE:FILENAME		
			case "DELETE":
				filename = split[1];
				return new DeletePacketHeader(port,filename);
		}
		return null;
	}

	
}
