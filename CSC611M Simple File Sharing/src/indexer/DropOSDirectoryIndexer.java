package indexer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

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
				
				indexEntry = "> " + filePath.getName() + " (" + filePath + ")";
				 
				indexEntry += " : [FileSize] - " + attributes.size() + " bytes";
				//indexEntry += " : [CreationTime] - " + attributes.creationTime();
				//indexEntry += " : [LastAccessTime] - " + attributes.lastAccessTime();
				indexEntry += " : [LastModifiedTime] - " + attributes.lastModifiedTime();
				
				//System.out.println(indexEntry);
				index.add(indexEntry);
			}
		}
	}
	
	
	public ArrayList<String> getDirectoryIndexList() {
		return index;
	}
	
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
}
