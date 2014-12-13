package indexer;

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
		sb.append("Resolution: \n\n");
		for (String file : keySet()){
			sb.append(file + " - " + get(file) + "\n");
		}
		return sb.toString();
	}
}

