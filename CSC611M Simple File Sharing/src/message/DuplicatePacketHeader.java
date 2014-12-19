package message;

import java.io.IOException;
import java.util.ArrayList;

import dropos.Config;

public class DuplicatePacketHeader extends FilePacketHeader {

	String duplicateheader;
	String command;
	ArrayList<String> ipAddresses = new ArrayList<String>();
	private int port;

	public DuplicatePacketHeader(int port, String header) {
		super(port, header);
		this.port = port;

		String[] split = header.split("\n");
		this.header = split[1];

		duplicateheader = split[0];
		split = duplicateheader.split(":");
		for (int i = 0; i < split.length; i++) {
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
		return Config.getInstancePath(port) + "\\" + filename;
	}

	public void duplicateRedundancy(DropOSProtocol protocol) {
		for (String ip : ipAddresses) {
			PacketHeader updatepacket = PacketHeader.create("UPDATE:" + filesize + ":" + filename, port);
			try {
				protocol.sendFile(updatepacket, file);
			} catch (IOException e) {
				System.err.println("Could not send " + file.toPath() + " to IP Address " + ip);
			}
		}
	}
}
