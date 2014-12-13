package indexer;

/**
 * The {@link FileAndLastModifiedPair} is a data structure used to sort the {@link Index} by their date of last modification.
 * 
 * @author Darren
 *
 */
public class FileAndLastModifiedPair {
	String file;
	Long dateModified;

	public FileAndLastModifiedPair(String file, Long dateModified) {
		this.file = file;
		this.dateModified = dateModified;
	}
}