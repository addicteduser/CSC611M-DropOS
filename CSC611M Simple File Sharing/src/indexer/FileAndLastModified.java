package indexer;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class FileAndLastModified extends HashMap<String, Long>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<FileAndLastModifiedPair> list = new ArrayList<FileAndLastModified.FileAndLastModifiedPair>();
	StringBuilder sb;
	private int toStringCount = -1;
	
	private class FileAndLastModifiedPair {
		String file;
		Long dateModified;
		
		public FileAndLastModifiedPair(String file, Long dateModified) {
			this.file = file;
			this.dateModified = dateModified; 
		}
	}
	
	
	
	public void sort(){
		list.clear();
		for (String file : keySet()){
			FileAndLastModifiedPair e = new FileAndLastModifiedPair(file, get(file));
			list.add(e);
		}
		
		Collections.sort(list, new IndexComparator());
	}
	
	@Override
	public String toString() {
		if (toStringCount == list.size())
			return sb.toString();
		
		sb = new StringBuilder();
		sb = sb.append("Index list:\n");
		
		sort();
		
		for (FileAndLastModifiedPair pair :  list ){
			Long lastModified = pair.dateModified;
			String file = pair.file;
			
			Date date = new Date(lastModified);
			String format = DateFormat.getInstance().format(date);
			
			sb.append(format + " - " + file + "\n");
		}
		
		return sb.toString();
	}
	
	private class IndexComparator implements Comparator<FileAndLastModifiedPair> {
		public int compare(FileAndLastModifiedPair a, FileAndLastModifiedPair b) {
			if (a.dateModified < b.dateModified)
				return -1;
			if (a.dateModified > b.dateModified)
				return 1;
			return 0;
		}
 
		
	}
}