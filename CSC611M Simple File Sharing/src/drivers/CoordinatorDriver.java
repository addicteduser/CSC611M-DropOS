package drivers;

import java.io.IOException;

import dropos.Config;
import dropos.DropCoordinator;

public class CoordinatorDriver {
	
	public static void main(String[] args) throws IOException {
		Config.initialize();
		
		DropCoordinator coordinator = DropCoordinator.create();
		Thread thread = new Thread(coordinator);
		thread.start();
	}
}
