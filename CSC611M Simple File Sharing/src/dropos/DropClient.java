package dropos;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;

public class DropClient extends Thread {
	private static int port;
	private static String hostname;
	private static Socket client;
	private static DataInputStream in;
	private static OutputStream out;
	private Path clientPath;

	public DropClient() {
		this.hostname = Config.getIpAddress();
		this.port = Config.getPort();
		this.clientPath = Config.getPath();
		System.out.println("[SYSTEM] Created new CLIENT instance");

		try {
			// create connection
			System.out.println("[CLIENT] CLIENT trying to connect to SERVER at IP:" + hostname + " on port " + port);
			client = new Socket(hostname, port);
			System.out.println("[CLIENT] CLIENT just connected to SERVER. CLIENT has IP:" + client.getInetAddress().toString());

			// instantiate connections to get input from CLIENT
			in = new DataInputStream(client.getInputStream());
			// instantiate connections to get input from CLIENT
			out = client.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		//DirectoryWatcher.watchDirectoryPath(clientPath, out);
		try {
			File myFile = clientPath.resolve("test.txt").toFile();
			FileInputStream fis = new FileInputStream(myFile);
			byte [] mybytearray  = new byte [(int)myFile.length()];
			BufferedInputStream bin = new BufferedInputStream(fis);
			bin.read(mybytearray, 0, mybytearray.length);
			out.write(mybytearray, 0, mybytearray.length);
			out.flush();
			/*out.writeLong(myFile.length()); // send how large the file is
			int n = 0;
			while ((n = fis.read(mybytearray)) != -1) {
				out.write(mybytearray, 0, n);
				out.flush();
			}*/
			fis.close();
			client.close();
			System.out.println("Done.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(true);
	}

}
