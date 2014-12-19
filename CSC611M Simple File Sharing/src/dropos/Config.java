package dropos;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Config {
	private static Path path;
	private static String ipAddress;
	private static int port;
	
	public static void initialize(){
		try {
			File f = new File("config.ini");
			Scanner s = new Scanner(f);
			
			while (s.hasNextLine()){
				String currentLine = s.nextLine().trim();
				String[] pair = currentLine.split("=");
				String var = pair[0].trim().toUpperCase();
				String value = pair[1].trim();
				switch(var){
				case "PATH": 
					path = Paths.get(System.getProperty("user.dir")).resolve(value);
				break;
				case "COORDINATOR IP":
					ipAddress = value;
					break;
				case "PORT":
					port = Integer.parseInt(value);
					break;
				default: 
					System.err.println("Unknown variable [" + var + "] being assigned with " + value);
					break;
				}
			}

			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Since we can run multiple clients on one computer, the instance path is the folder 
	 * of the designated host. They are differentiated by their port number. Therefore,
	 * the hosts have their own separate 'instance folders' labeled by their port number.
	 *  
	 *  Note, the folders should not be specified as 'client' because these are used by all 
	 *  hosts whether Client, Coordinator, or Server.
	 *  
	 * @param port
	 * @return
	 */
	public static Path getInstancePath(int port){
		return path.resolve(port+"\\");
	}
	
	public static Path getAbsolutePath() {
		return path;
	}

	public static String getIpAddress() {
		return ipAddress;
	}

	public static int getPort() {
		return port;
	}
}
