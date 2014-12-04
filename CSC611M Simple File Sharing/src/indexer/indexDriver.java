package indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class indexDriver {
	public static void main(String args[]) {
		DropOSDirectoryIndexer dodi = new DropOSDirectoryIndexer();
		
		File theFile = new File("./src");
		
		//dodi.listFilesAndFilesSubDirectories(theFile);
		try {
			dodi.indexServerDirectory(theFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//dodi.getDirectoryIndexList();
		File wow = dodi.getDirectoryIndexListFile();
		
		
		String line = null;
		try {
			// Construct BufferedReader from FileReader
			BufferedReader br = new BufferedReader(new FileReader(wow));
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
		
	}

}
