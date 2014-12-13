package message;

import java.io.File;
import java.io.IOException;

import dropos.Config;

public class IndexListPacketHeader extends FilePacketHeader {

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
	
	/**
	 * The index of the server is labeled with the server's IP, and is not stored in the logically synchronized folder.
	 */
	protected String filePath() {
		return Config.getIpAddress() + ".txt";
	}

}
