package indexer;

import java.io.File;
import java.io.IOException;

public class IndexDriver {
	public static void main(String args[]) {
		DropOSDirectoryIndexer dodi = new DropOSDirectoryIndexer();

		File theFile = new File("./src");

		// dodi.listFilesAndFilesSubDirectories(theFile);
		try {
			dodi.indexServerDirectory(theFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// dodi.getDirectoryIndexList();
		File wow = dodi.getDirectoryIndexListFile();

		// String line = null;
		// try {
		// // Construct BufferedReader from FileReader
		// BufferedReader br = new BufferedReader(new FileReader(wow));
		// while ((line = br.readLine()) != null) {
		// System.out.println(line);
		// }
		// br.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		FileAndLastModified hm = new FileAndLastModified();
		FileAndAction resolution = new FileAndAction();

		FileAndLastModified serverIndexList = new FileAndLastModified();
		FileAndLastModified clientIndexList = new FileAndLastModified();

		serverIndexList.put("File A", Long.parseLong("123745312690000"));
		serverIndexList.put("File B", Long.parseLong("123745312690001"));

		clientIndexList.put("File A", Long.parseLong("123745312630023"));
		clientIndexList.put("File B", Long.parseLong("123745312630024"));
		clientIndexList.put("File C", Long.parseLong("123745312630025"));
		
		System.out.println(serverIndexList);
		
		System.out.println(clientIndexList);
		//
		// hm = dodi.extractInfoFromIndexListFile(wow);

		// Set<Entry<String, Long>> set = hm.entrySet();
		// Iterator<Entry<String, Long>> i = set.iterator();
		//
		// while(i.hasNext()) {
		// Map.Entry me = (Map.Entry)i.next();
		// System.out.print("FILENAME: " + me.getKey() + " : ");
		// System.out.println("LAST MODIFIED TIME: " + me.getValue());
		// }

		resolution = dodi.compare(serverIndexList, clientIndexList);

		for (String file : resolution.keySet()) {
			String action = resolution.get(file);
			System.out.print("FILENAME: " + file + " : ");
			System.out.println("ACTION: " + action);
		}
	}

}
