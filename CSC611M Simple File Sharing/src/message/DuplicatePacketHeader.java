package message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.LongPredicate;

import dropos.Config;

public class DuplicatePacketHeader extends FilePacketHeader{
	
	String duplicateheader;
	String command;
	ArrayList<String> ipAddresses = new ArrayList<String>();

	public DuplicatePacketHeader(String header) {
		super(header);
		String[] split = header.split("\n");
		this.header = split[1];
		
		duplicateheader = split[0];
		split = duplicateheader.split(":");
		for(int i = 0; i < split.length; i++) {
			if (i == 0)
				command = split[i];
			else
				ipAddresses.add(split[i]);
		}
		
		filesize = Long.parseLong(this.header.split(":")[1]);
		filename = this.header.split(":")[2];
	}
	
	@Override
	protected String filePath() {
		return Config.getPath() + "\\" + filename;
	}
	
	public void duplicateRedundancy(DropOSProtocol protocol) {
		for(String ip : ipAddresses) {
			PacketHeader updatepacket = PacketHeader.create("UPDATE:"+filesize+":"+filename);
			// protocol.sendFile(updatepacket, f);
		}
	}
}
