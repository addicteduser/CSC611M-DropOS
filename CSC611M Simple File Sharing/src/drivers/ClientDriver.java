package drivers;

import java.io.IOException;

import dropos.Config;
import dropos.DropClient;

public class ClientDriver {
	public static void main(String[] args) throws IOException {
		Config.initialize();
		DropClient c = DropClient.create();
		
		Thread thread = new Thread(c);
		thread.start();
	}

}
