package message;

import java.io.File;
import java.io.IOException;

import dropos.Config;

public class FilePacketHeader extends PacketHeader {

	protected long filesize;
	protected String filename;

	public FilePacketHeader(String header) {
		super(header);
		String[] split = header.split(" ");
		
		try {
			filesize = Long.parseLong(split[1]);
			filename = split[2];
		}catch(ArrayIndexOutOfBoundsException e){ 
			// This is fine, the index has less parameters
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
	protected File receiveFile(DropOSProtocol dropOSProtocol) throws IOException{
		return dropOSProtocol.receiveFile(filePath(), filesize);
	}
	
	/**
	 * All files are stored in the logically synchronized folder.
	 * @return
	 */
	protected String filePath(){
		return Config.getPath() + "\\" + filename;
	}

	@Override
	public Message interpret(DropOSProtocol protocol) throws IOException {
		File file = receiveFile(protocol);
		System.out.println("File was received.");
		FileAndMessage fileAndMessage = new FileAndMessage("INDEX", file);
		return fileAndMessage;
	}

}
