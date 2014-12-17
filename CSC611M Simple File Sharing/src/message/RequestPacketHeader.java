package message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dropos.Config;

public class RequestPacketHeader extends FilePacketHeader {
	
	public RequestPacketHeader(String header) throws IOException {
		super(header);
		String[] split = header.split(" ");
		String command = split[0];
		//get the file
		Path path = Config.getPath();
		filename = split[1];
		File f = new File(path + "\\" + filename);
		System.out.println("REQUESTED FILE: " + f.toPath());
		//get the file size
		filesize = Files.size(f.toPath());
		
		header = command + " " + filesize + " " + f.getName(); 
		
		
		// build the packet header
		// send the file
		// TODO Auto-generated constructor stub
	}

}
