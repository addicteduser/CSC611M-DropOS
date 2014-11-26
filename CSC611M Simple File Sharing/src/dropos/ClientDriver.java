package dropos;

import java.io.IOException;

public class ClientDriver {
	public static void main(String[] args) throws IOException {
		DropClient c = new DropClient(HOSTNAME, PORT_NUMBER);
		c.start();
	}
}
