package drivers;

import indexer.Index;
import indexer.Resolution;

import java.io.IOException;

import dropos.Config;
import dropos.DropClient;

public class ClientDriver {
	public static void main(String[] args) throws IOException {

		// When the application is exited properly (closing the window), the index is updated and written down.
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {

			}
		}, "Shutdown-thread"));

		Config.initialize();
		DropClient c = DropClient.create();
		
		Thread thread = new Thread(c);
		thread.start();
	}

}
