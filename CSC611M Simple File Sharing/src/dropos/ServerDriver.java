package dropos;

import java.io.IOException;

public class ServerDriver {
	
	private static final int PORT_NUMBER = 4040;
	private static final String HOSTNAME = "localhost";
	
	public static void main(String[] args) throws IOException {
		Config.initialize();
		
		DropServer s = new DropServer(PORT_NUMBER);
		s.start();
	}
}
