package message;

import java.io.File;
import java.io.IOException;

import dropos.Config;

public class IndexListPacketHeader extends PacketHeader {

	long filesize = 0;
	/**
	 * This method is used when you receive a packet header that is for an index list. 
	 * @param header
	 */
	public IndexListPacketHeader(String header) {
		super(header);
		try {
			filesize = Long.parseLong(header.split(" ")[1]);	
		}catch (Exception e){
			System.err.println("Failed to parse filesize.");
		}
	}
	
	/**
	 * This method is used when you are about to send an index list, and you are preparing the stream's packet header.
	 */
	public IndexListPacketHeader() {
		super("INDEX ");
		try {
			filesize = Long.parseLong(header.split(" ")[1]);	
		}catch (Exception e){
			System.err.println("Failed to parse filesize.");
		}
		
		// Append filesize of index list
		header += filesize;
	}

	public File receiveIndex(DropOSProtocol dropOSProtocol) throws IOException{
		// Label the server's Index file with its IP address
		// e.g. '192.168.10.1.txt'
		
		String filename = Config.getIpAddress() + ".txt";
		return dropOSProtocol.receiveFile(filename, filesize);
	}

}
