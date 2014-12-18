package message;

import indexer.Index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;

import message.DropOSProtocol.HostType;
import dropos.Config;

public class IndexListPacketHeader extends FilePacketHeader {

	/**
	 * This method is used when you receive a packet header that is for an index list. 
	 * @param header
	 */
	public IndexListPacketHeader(String header) {
		super(header);
		try {
			filesize = Long.parseLong(header.split(":")[1]);	
		}catch (Exception e){
			System.err.println("Failed to parse filesize.");
		}
	}
	
	/**
	 * This method is used when you are about to send an index list, and you are preparing the stream's packet header.
	 */
	public IndexListPacketHeader() {
		super("INDEX:");
		try {
			File file = Index.getInstance().getFile();
			BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			
			filename = file.getName();
			filesize = file.length();	
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

	
	public Message interpret(DropOSProtocol protocol) throws IOException {
		File file = receiveFile(protocol);
		System.out.println("File " + file.getName() + " was received.");
		return new FileAndMessage("INDEX", file);
	}
}
