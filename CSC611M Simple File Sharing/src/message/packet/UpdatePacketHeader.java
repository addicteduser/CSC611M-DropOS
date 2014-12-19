package message.packet;

import java.nio.file.Path;
import java.nio.file.Paths;

public class UpdatePacketHeader extends FilePacketHeader {

	public UpdatePacketHeader(int port, String filename) {
		super(port, "UPDATE");
		this.filename = filename;
		
		String path = filePath();
		Path p = Paths.get(path);
		this.filesize = p.toFile().length();
		
		this.header += ":" + filesize;
		
		this.header += ":" + filename;
	}
	
	

}
