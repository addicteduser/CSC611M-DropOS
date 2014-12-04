package indexer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
		
		
//		String line = null;
//		try {
//			// Construct BufferedReader from FileReader
//			BufferedReader br = new BufferedReader(new FileReader(wow));
//			while ((line = br.readLine()) != null) {
//				System.out.println(line);
//			}
//			br.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		HashMap<String,Long> hm = new HashMap<String,Long>();
		HashMap<String,String> hm2 = new HashMap<String,String>();
		
		HashMap<String,Long> server = new HashMap<String,Long>();
		HashMap<String,Long> client = new HashMap<String,Long>();
		
		server.put("File A", Long.parseLong("123745312630000"));
		server.put("File B", Long.parseLong("123745312630050"));
		
		client.put("File A", Long.parseLong("123745312630023"));
		client.put("File B", Long.parseLong("123745312630023"));
		client.put("File C", Long.parseLong("123745312630023"));
//	    
//		hm = dodi.extractInfoFromIndexListFile(wow);
		
//		Set<Entry<String, Long>> set = hm.entrySet();
//	    Iterator<Entry<String, Long>> i = set.iterator();
//	     
//	    while(i.hasNext()) {
//	         Map.Entry me = (Map.Entry)i.next();
//	         System.out.print("FILENAME: " + me.getKey() + " : ");
//	         System.out.println("LAST MODIFIED TIME: " + me.getValue());
//	    }
		
		hm2 = dodi.compareIndexLists(server, client);
		
		Set<Entry<String,String>> set = hm2.entrySet();
	    Iterator<Entry<String, String>> i = set.iterator();
	     
	    while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         System.out.print("FILENAME: " + me.getKey() + " : ");
	         System.out.println("ACTION: " + me.getValue());
	    }
	}

}
