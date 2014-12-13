package indexer;

/**
 * The {@link FileAndLastModifiedPair} is a data structure used to sort the {@link Index} by their date of last modification.
 * 
 * @author Darren
 *
 */
public class FileAndLastModifiedPair {
	String file;
	Long lastModified;

	public FileAndLastModifiedPair(String file, Long dateModified) {
		this.file = file;
		this.lastModified = dateModified;
	}
	
	@Override
	/**
	 * This method returns true if the filenames are identical.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof FileAndLastModifiedPair){
			FileAndLastModifiedPair o = (FileAndLastModifiedPair) obj;
			return o.file.equals(file);
		}
		return false;
	}
	
	/**
	 * This method returns true if the filename and the date of last modification is exactly the same. 
	 * @param pair
	 * @return true if they are exactly the same
	 */
	public boolean exactlyEquals(FileAndLastModifiedPair pair){
		return file == pair.file && lastModified == pair.lastModified;
	}
}