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
				String currentLine = s.nextLine().trim().toUpperCase();
				String[] pair = currentLine.split("=");
				String var = pair[0].trim();
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

	public static Path getPath() {
		return path;
	}

	public static String getIpAddress() {
		return ipAddress;
	}

	public static int getPort() {
		return port;
	}
}
