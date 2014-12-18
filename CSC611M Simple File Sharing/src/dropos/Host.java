package dropos;

public class Host {
	String ipAddress;
	int port;
	
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
}
