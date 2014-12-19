package message.packet;

import java.nio.file.Path;

import dropos.Config;


public class UpdatePacketHeader extends FilePacketHeader {

	public UpdatePacketHeader(int port, long filesize, String filename) {
		super(port, "UPDATE");
		this.filename = filename;
		this.filesize = filesize;
		
		this.header += ":" + filesize;
		
		this.header += ":" + filename;
	}
	
	
	@Override
	protected String filePath() {
		Path path = Config.getInstancePath(port);
		return path.resolve(filename).toString();
	}
	

}
