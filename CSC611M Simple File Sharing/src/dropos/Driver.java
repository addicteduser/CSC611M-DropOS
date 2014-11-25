package dropos;

import java.io.IOException;

public class Driver {
	
	private static final int PORT_NUMBER = 4040;
	private static final String HOSTNAME = "localhost";
	
	public static void main(String[] args) throws IOException {
		DropServer s = new DropServer(PORT_NUMBER);
		s.start();
		
		DropClient c = new DropClient(HOSTNAME, PORT_NUMBER);
		c.start();
	}
}
