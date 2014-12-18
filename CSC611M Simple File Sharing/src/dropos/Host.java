package dropos;

import java.net.Socket;
import java.util.concurrent.Semaphore;

import message.DropOSProtocol;

public class Host {
	public enum HostType {
		Server, Client
	}
	private DropOSProtocol protocol;
	private String ipAddress;
	private int port = -1;
	private HostType type = null;
	private Semaphore mutexLock;
	
	
	public Host(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
		mutexLock = new Semaphore(1);
	}
	
	public Host(Socket connectionSocket) {
		if (connectionSocket.equals(null))
		{
			System.err.println("Connection socket passed to Host is null. System is now exiting...");
			System.exit(1);
		}
		
		ipAddress = connectionSocket.getInetAddress().toString().substring(1);
		port = connectionSocket.getLocalPort();
		mutexLock = new Semaphore(1);
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
			System.err.println("PortError. Cannot re-set the port of a host.");
	}
	
	public void acquire() throws InterruptedException{
		mutexLock.acquire();
	}
	
	public void release(){
		mutexLock.release();
	}

	/**
	 * Returns true of the Host is the same Host, or if the Socket provided deals with the same host.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Socket){
			Socket socket = (Socket) obj;
			return socket.getInetAddress().equals(ipAddress) && port == socket.getPort();
		}
		return super.equals(obj);
	}
	
	@Override
	public String toString() {
		if (type != null)
			return "(" + type.toString() + ")" + ipAddress + ":" + port; 
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
	
	/**
	 * This method creates a new socket connection to the desired host. It then provides the {@link DropOSProtocol} instance as a return value. 
	 * @return
	 */
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
