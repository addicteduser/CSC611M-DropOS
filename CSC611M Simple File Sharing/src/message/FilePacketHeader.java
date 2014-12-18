package message;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import dropos.Config;

public class FilePacketHeader extends PacketHeader {

	protected long filesize;
	protected String filename;
	protected File file;

	public FilePacketHeader(String header) {
		super(header);
		String[] split = header.split(":");
		
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
		return Config.getPath() + "\\temp\\" + filename;
	}

	@Override
	public Message interpret(DropOSProtocol protocol) throws IOException {
		file = receiveFile(protocol);
		System.out.println("File was received.");
		return new FileAndMessage("UPDATE " + filename, file);
	}
	
	public void writeFile(){
		File temporaryFile = new File(filePath());
		File actualFile = new File(Config.getPath() + "\\" + filename);
		try {
			Files.copy(temporaryFile.toPath(), actualFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("Failed to copy file to actual directory: " + actualFile.getName());
		}
	}

}
