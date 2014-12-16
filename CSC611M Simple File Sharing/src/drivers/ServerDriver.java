package drivers;

import java.io.IOException;

import message.DropOSProtocol;
import message.DropOSProtocol.HostType;
import dropos.Config;
import dropos.DropServer;

public class ServerDriver {
	
	public static void main(String[] args) throws IOException {
		Config.initialize();
		
		DropOSProtocol.type = HostType.Server;
		DropServer s = new DropServer(Config.getPort());
		s.run();
	}
}
