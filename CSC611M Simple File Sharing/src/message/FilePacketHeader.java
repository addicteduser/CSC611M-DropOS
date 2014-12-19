package message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import dropos.Config;

public class FilePacketHeader extends PacketHeader {

	protected long filesize;
	protected String filename;
	protected File file;

	public FilePacketHeader(int port, String header) {
		super(port, header);
		String[] split = header.split(":");
		
		try {
			filesize = Long.parseLong(split[1]);
			filename = split[2];
		}catch(ArrayIndexOutOfBoundsException e){ 
			// This is fine, the index has less parameters
		}catch (Exception e){
			// 
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
		return Config.getInstancePath(port).resolve("\\temp\\" + filename).toString();
	}

	@Override
	public Message interpret(DropOSProtocol protocol) throws IOException {
		file = receiveFile(protocol);
		return new FileAndMessage("UPDATE:" + file.length() + ":" + filename, file);
	}
	
	public void writeFile(int port){
		File temporaryFile = new File(filePath());
		Path portInstancePath = Paths.get(port+"\\");
		if (Files.exists(portInstancePath, LinkOption.NOFOLLOW_LINKS) == false){
			try {
				Files.createDirectory(portInstancePath);
			} catch (IOException e) {
				System.out.println("Could not create folder " + portInstancePath);
			}
		}
		
		File actualFile = new File(Config.getInstancePath(port) + "\\" + filename);
		
		try {
			Files.copy(temporaryFile.toPath(), actualFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("Failed to copy file to actual directory: " + actualFile.getName());
		}
	}

}
