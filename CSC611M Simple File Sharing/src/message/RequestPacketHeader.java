package message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dropos.Config;

public class RequestPacketHeader extends FilePacketHeader {

	public RequestPacketHeader(String header) {
		super(header);
		String[] split = header.split(" ");

		try {
			filesize = Long.parseLong(split[2]);
			filename = split[1];
		}catch(ArrayIndexOutOfBoundsException e){ 
			// This is fine, the index has less parameters
		}catch (Exception e){
			System.err.println("Failed to parse filesize.");
		}
	}
	
	public Message interpret(DropOSProtocol protocol) throws IOException {
		File file = receiveFile(protocol);
		System.out.println("Received requested file.");
		return new FileAndMessage("UPDATE " + filename, file);
	}

}
