package dropos;

import java.io.IOException;

public class ServerDriver {
	
	public static void main(String[] args) throws IOException {
		Config.initialize();
		
		DropServer s = new DropServer(Config.getPort());
		s.start();
	}
}
