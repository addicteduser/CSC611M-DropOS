package message;

import java.io.File;
import java.io.IOException;

import dropos.Config;

public class FilePacketHeader extends PacketHeader {

	private long filesize;
	private String filename;

	public FilePacketHeader(String header) {
		super(header);
		String[] split = header.split(" ");
		filename = split[2];
		try {
			filesize = Long.parseLong(split[1]);	
		}catch (Exception e){
			System.err.println("Failed to parse filesize.");
		}
	}
	
	/**
	 * This method receives a file and places it in the designated synchronized directory.
	 * @param dropOSProtocol the current connection to the server
	 * @return the file sent from the server
	 * @throws IOException
	 */
	public File receiveFile(DropOSProtocol dropOSProtocol) throws IOException{
		return dropOSProtocol.receiveFile(filePath(), filesize);
	}
	
	/**
	 * All files are stored in the logically synchronized folder.
	 * @return
	 */
	protected String filePath(){
		return Config.getPath() + "\\" + filename;
	}

}
