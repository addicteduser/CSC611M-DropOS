package drivers;

import java.io.IOException;

import message.DropOSProtocol;
import message.DropOSProtocol.HostType;
import dropos.Config;
import dropos.DropServer;

public class ServerDriver {

	public static void main(String[] args) throws IOException {
		Config.initialize();

		DropServer s = DropServer.create();
		s.run();
	}

}
