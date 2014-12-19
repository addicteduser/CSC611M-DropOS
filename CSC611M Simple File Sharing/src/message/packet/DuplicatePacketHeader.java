package message.packet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import message.DropOSProtocol;
import dropos.Config;
import dropos.Host;

public class DuplicatePacketHeader extends FilePacketHeader {

	String redundancyHeader;
	String updateHeader;
	ArrayList<String> ipAddresses = new ArrayList<String>();

	/**
	 * This method is used to create the DuplicatePacketHeader class.
	 * @param port
	 * @param filename
	 * @param size
	 * @param redundantServers
	 */
	public DuplicatePacketHeader(int port, String filename, long size, ArrayList<Host> redundantServers) {
		super(port, "DUPLICATE");
		
		filesize = size;
		this.filename = filename;
		
		// Prepare redundancy header by concatenating the IP Addresses of the servers
		for(Host h : redundantServers) {
			header += ":" + h.getIpAddress() + "-"+ h.getPort();
		}
		redundancyHeader = header;
		
		// Separate the headers using a line break
		header += "\n";
		
		// Prepare update header by specifying the update details
		header += "UPDATE:" + size + ":" + filename;
	}
	
	/**
	 * This method is used to parse the DuplicatePacketHeader class from a string.
	 * @param port
	 * @param header
	 */
	public DuplicatePacketHeader(int port, String header) {
		super(port, header);

		String[] split = header.split("\n");
		this.header = split[1];

		redundancyHeader = split[0];
		split = redundancyHeader.split(":");
		for (int i = 0; i < split.length; i++) {
			if (i == 0)
				updateHeader = split[i];
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
		long filesize = file.length();
		for (String ip : ipAddresses) {
			UpdatePacketHeader updatepacket = PacketHeader.createUpdate(filename, filesize, port);
			
			try {
				protocol.sendFile(updatepacket, file);
			} catch (IOException e) {
				System.err.println("Could not send " + file.toPath() + " to IP Address " + ip);
			}
		}
	}
}
