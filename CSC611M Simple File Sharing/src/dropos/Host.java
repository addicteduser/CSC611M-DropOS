package dropos;

import java.io.IOException;
import java.net.Socket;

import message.DropOSProtocol;

public class Host {
	public enum HostType {
		Server, Client
	}
	private DropOSProtocol protocol;
	private String ipAddress;
	private int port = -1;
	private HostType type = null;
		
	public Host(String ipAddress, int port){	
		this.ipAddress = ipAddress;
		this.port = port;
	}
	public Host(Socket socket) {
		if (socket.equals(null))
		{
			System.err.println("Connection socket passed to Host is null. System is now exiting...");
			System.exit(1);
		}
		
		ipAddress = socket.getInetAddress().toString().substring(1);
	}
	
	public void setType(HostType type){
		if (this.type == null)
			this.type = type;
		else
			System.err.println("TypeError. Cannot re-set the type of a host.");
	}
	
	public void setPort(int port){
		if (this.port == -1)
			this.port = port;
		else
			System.err.println("PortError. Cannot re-set the port of a host " + ipAddress + " to " + port + " because it is already " + this.port);
	}
	
	/**
	 * Returns true of the Host is the same Host, or if the Socket provided deals with the same host.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Host){
			Host host = (Host) obj;
			return ipAddress.equalsIgnoreCase(host.ipAddress) && host.port == port;
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder name = new StringBuilder();
		
		if (type != null)
			name.append("(" + type.toString() + ")");
		
		name.append(ipAddress);
		
		if (port > 0)
			name.append( ":" + port );
		
		return name.toString();
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public int getPort() {
		return port;
	}

	public DropOSProtocol getProtocol() {
		return protocol;
	}
	
	/**
	 * This method creates a new socket connection to the desired host. It then provides the {@link DropOSProtocol} instance as a return value. 
	 * @return
	 */
	public DropOSProtocol createProtocol(){
		Socket s = null;
		try {
			s = new Socket(ipAddress, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return createProtocol(s);
	}
	
	public DropOSProtocol createProtocol(Socket s){
		DropOSProtocol protocol = null;
		try {
			protocol = new DropOSProtocol(s);
		}catch(Exception e){
			System.err.println("Could not create a connection with the host " + toString() + " using socket " + s);
			e.printStackTrace();
		}
		return protocol;
	}
}
