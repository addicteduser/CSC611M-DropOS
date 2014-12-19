package message.packet;

import java.io.IOException;
import java.util.ArrayList;

import message.DropOSProtocol;
import message.Message;
import dropos.Config;
import dropos.Host;

public class DuplicatePacketHeader extends FilePacketHeader {

	String redundancyHeader;
	String updateHeader;
	ArrayList<Host> hosts;

	/**
	 * This method is used to create the DuplicatePacketHeader class.
	 * @param port
	 * @param filename
	 * @param size
	 * @param redundantServers
	 */
	public DuplicatePacketHeader(int port, String filename, long size, ArrayList<Host> redundantServers) {
		super(port, "DUPLICATE");
		hosts = redundantServers;
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
		hosts = new ArrayList<Host>();
		
		String[] split = header.split("\n");
		
		// The two headers
		redundancyHeader = split[0];
		updateHeader = split[1];
		
		// Update part
		String[] updateSplit = updateHeader.split(":");
		filesize = Long.parseLong(updateSplit[1]);
		filename = updateSplit[2];

		// Duplicate part
		split = redundancyHeader.split(":");
		
		// Parsing the hosts
		for (int i = 1; i < split.length; i++) {
			String[] pair = split[i].split("-");
			Host h = new Host(pair[0], Integer.parseInt(pair[1]));
			hosts.add(h);
		}

		
	}

	@Override
	protected String filePath() {
		return Config.getInstancePath(port) + "\\" + filename;
	}
	
	@Override
	public Message interpret(DropOSProtocol protocol) throws IOException {
		Message message = super.interpret(protocol);
		duplicateRedundancy(protocol);
		return message;
	}

	public void duplicateRedundancy(DropOSProtocol protocol) {
		long filesize = file.length();
		for (Host h : hosts) {
			UpdatePacketHeader updatepacket = PacketHeader.createUpdate(filename, filesize, port);
			
			try {
				System.out.println("[Duplicate] file " + file.getName() + " to server " + h);
				System.out.println(updatepacket);
				System.out.println();
				DropOSProtocol createProtocol = h.createProtocol();
				createProtocol.sendFile(updatepacket, file);
			} catch (IOException e) {
				System.err.println("Could not send " + file.toPath() + " to host " + h);
			}
		}
	}
}
