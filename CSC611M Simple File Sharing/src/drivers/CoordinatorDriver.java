package drivers;

import java.io.IOException;

import dropos.Config;
import dropos.DropCoordinator;

public class CoordinatorDriver {
	
	public static void main(String[] args) throws IOException {
		Config.initialize();
		
		DropCoordinator coordinator = new DropCoordinator(Config.getPort());
		coordinator.run();
	}
}
