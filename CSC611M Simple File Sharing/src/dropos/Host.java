package dropos;

import java.net.Socket;

import message.DropOSProtocol;

public class Host {
	private DropOSProtocol protocol;
	private String ipAddress;
	private int port;
	
	
	public Host(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	@Override
	public String toString() {
		return ipAddress + ":" + port;
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
	
	public DropOSProtocol createProtocol(){
		DropOSProtocol protocol = null;
		try {
			Socket s = new Socket(ipAddress, port);
			protocol = new DropOSProtocol(s);
		}catch(Exception e){
			System.err.println("Could not create a connection with the host " + toString());
			e.printStackTrace();
		}
		return protocol;
	}

}
