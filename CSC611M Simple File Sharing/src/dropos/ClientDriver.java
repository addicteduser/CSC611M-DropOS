package dropos;

import java.io.IOException;

public class ClientDriver {
	public static void main(String[] args) throws IOException {
		Config.initialize();
		DropClient c = new DropClient(Config.getIpAddress(), Config.getPort());
		c.start();
	}
}
