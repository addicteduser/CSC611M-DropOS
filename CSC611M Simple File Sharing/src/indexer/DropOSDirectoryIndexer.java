package indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DropOSDirectoryIndexer {
	private File[] directoryFilePaths;
	private ArrayList<String> index = new ArrayList<String>();
	
	public DropOSDirectoryIndexer() {
	}
	
	public void indexServerDirectory(File serverDirectoryFile) throws IOException {
		BasicFileAttributes attributes;
		String indexEntry;
		directoryFilePaths = serverDirectoryFile.listFiles();
		
		for(File filePath : directoryFilePaths) {
			
			if(filePath.isDirectory()) {
				//System.out.println("<+> " + filePath.getName() + " (" + filePath + ")");
				//index.add(filePath.getPath());
				indexServerDirectory(filePath);
			} else {
				attributes = Files.readAttributes(filePath.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
				
				indexEntry = filePath.getName() + ":" + attributes.lastModifiedTime().toMillis();
				 
				//System.out.println(indexEntry);
				index.add(indexEntry);
			}
		}
	}
	
	
	public ArrayList<String> getDirectoryIndexList() {
		return index;
	}
	
	// RETURNS THE TEXT FILE VERSION OF THE INDEX LIST FROM "indexServerDirectory(File theFile)"
	public File getDirectoryIndexListFile() {
		File indexList = new File("indexlist.txt");
		
		try {
			FileOutputStream out = new FileOutputStream(indexList);
		 
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
			
			for(String s : index) {
				writer.write(s + "\n");
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return indexList;
	}
	
	// EXTRACTS THE FILENAMES AND TIMESTAMPS(LONG) FROM THE GIVEN INPUT DIRECTORY FILE
	public HashMap<String,Long> extractInfoFromIndexListFile(File indexFile) {
		HashMap<String,Long> info = new HashMap<String,Long>();
		String indexLine = null;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(indexFile));
			
			while ((indexLine = br.readLine()) != null) {
				String[] i = indexLine.split(":");
				
				info.put(i[0], Long.parseLong(i[1]));
			}
			
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return info;
	}
	
	public HashMap<String,String> compareIndexLists(HashMap<String,Long> server, HashMap<String,Long> client) {
		HashMap<String,String> actions = new HashMap<String,String>();
		
		Set<Entry<String,Long>> set = client.entrySet();
	    Iterator<Entry<String,Long>> iterator = set.iterator();
	    
	    while(iterator.hasNext()) {
	    	Map.Entry<String,Long> me = (Map.Entry<String,Long>)iterator.next();
	    	
	    	String key = me.getKey();
	    	long value = me.getValue();
	   
	    	if(server.containsKey(key)) {
	    		if(server.get(key) < value)
	    			actions.put(key, "REQUEST");
	    		else if(server.get(key) > value)
	    			actions.put(key, "ADD");
	    	} else {
	    		actions.put(key, "REQUEST");
	    	}
	    			
	    }
		
		return actions;
	}
	
}
