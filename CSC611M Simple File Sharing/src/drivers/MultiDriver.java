package drivers;

import dropos.Config;
import dropos.DropServer;

public class MultiDriver {

	public static void main(String[] args) {
		Config.initialize();

		DropServer s = DropServer.create();
		
		Thread thread = new Thread(s);
		thread.start();
	}

}
