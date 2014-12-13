package indexer;

import indexer.Index.FileDifference;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>A {@link Resolution} instance is the result of a comparison between two {@link Index}es. Simply, this contains a list of files. 
 * Each file is paired with an action: either UPDATE, REQUEST, DELETE, or NONE.</p>  
 * @author Darren
 *
 */
public class Resolution extends HashMap<String, String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nResolution: \n");
		for (String file : keySet()){
			sb.append(file + " - " + get(file) + "\n");
		}
		return sb.toString();
	}
	

	/**
	 * <p>
	 * This method is called when you wish to compare the differences between the index lists.
	 * Note that you should pass the more <b>older index list first</b>, then the updated index list last. 
	 * </p>
	 * 
	 * @param olderIndex
	 *            The Index file of the server
	 * @param updatedIndex
	 *            The Index file of the client
	 * @return A HashMap of files and their respective actions.
	 */
	public static Resolution compare(Index olderIndex, Index updatedIndex) {
		Resolution actions = new Resolution();
		
		ArrayList<FileAndLastModifiedPair> inspectedFiles = new ArrayList<FileAndLastModifiedPair>();
		
		for (FileAndLastModifiedPair pair : updatedIndex) {
			
			FileDifference fileDifference = olderIndex.containsPair(pair);
			
			String filename = pair.file;
			
			switch(fileDifference){
			case MISSING:
				// The older index does not have this file, therefore it is marked as a new file to be added / updated.
				actions.put(filename, "UPDATE");
				 break;
				 
			case UPDATED:
				// The older index has this file, but the newer index has a more updated copy. Therefore, the new file must be updated.
				actions.put(filename, "REQUEST");
				break;
				
			case OUTDATED:
				actions.put(filename, "UPDATE");
				break;
				
			case SAME:
				// There were no changes.
				actions.put(filename, "NONE");
				break;
			}
			
			inspectedFiles.add(pair);
		}
		
		// Inspect the older index if some files were not dealt with in the newer index.
		for(FileAndLastModifiedPair pair : olderIndex){
			
			// Ignore files that were dealt with already
			if(inspectedFiles.contains(pair)) continue;
			
			// If you found any, these are marked as deletions.
			String filename = pair.file;
			actions.put(filename, "DELETE");
		}

		return actions;
	}
}

