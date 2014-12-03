package drivers;

import java.io.IOException;

import dropos.Config;
import dropos.ConnectionHandler;

public class ServerDriver {
	
	public static void main(String[] args) throws IOException {
		Config.initialize();
		
		ConnectionHandler s = new ConnectionHandler(Config.getPort());
		s.start();
	}
}
