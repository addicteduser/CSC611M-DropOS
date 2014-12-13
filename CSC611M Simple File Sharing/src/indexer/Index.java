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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import dropos.Config;

/**
 * The {@link Index} holds a {@link HashMap} of file names and their date of last modification.
 * 
 * @author Kyle
 *
 */
public class Index extends ArrayList<FileAndLastModifiedPair> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Index instance;
	private static Index preStartup;
	
	private StringBuilder sb;
	private int toStringCount = -1;

	private File[] directoryFilePaths;
	
	public Index() {

	}

	/**
	 * <p>
	 * This method recursively indexes a directory. If it finds a file, it adds the file to the index. If it finds a folder, it performs the indexing on the
	 * whole directory.
	 * </p>
	 * 
	 * @param directory
	 *            The directory to be indexed
	 * @throws IOException
	 *             This error is thrown when the file is missing, or if there is an error.
	 */
	private void indexDirectory(File directory) throws IOException {
		BasicFileAttributes attributes;
		directoryFilePaths = directory.listFiles();

		for (File filePath : directoryFilePaths) {

			if (filePath.isDirectory()) {
				// Recursively index the detected folder
				indexDirectory(filePath);
			} else {

				// Get the attributes and add an index entry
				attributes = Files.readAttributes(filePath.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
				String filename = filePath.getName();
				long lastModified = attributes.lastModifiedTime().toMillis();
				
				put(filename, lastModified);
			}
		}
	}

	/**
	 * <p>
	 * This method exports the text file which contains the index list. It uses the indexes detected by the indexServerDirectory method.
	 * </p>
	 * 
	 * @return The file reference to the index list
	 */
	public File write() {
		File indexList = new File("indexlist.txt");

		try {
			FileOutputStream out = new FileOutputStream(indexList);

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

			for (FileAndLastModifiedPair pair : this) {
				String filename = pair.file;
				Long lastModified = pair.dateModified;
				writer.write(filename + ":" + lastModified + "\n");
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return indexList;
	}

	/**
	 * <p>
	 * This method allows you to produce an {@link Index} of a selected directory.
	 * </p>
	 * 
	 * <p>
	 * Usually, the parameter passed here is the directory specified in the {@link Config} class.
	 * </p>
	 * 
	 * @param directory
	 *            The directory to be indexed
	 * @return The index
	 */
	public static Index directory(File directory) {
		Index index = new Index();
		try {
			index.indexDirectory(directory);
		} catch (IOException e) {
			System.out.println("Indexing the given directory '" + directory.toString() + "' failed.");
			System.out.println();
			e.printStackTrace();
		}
		return index;
	}

	/**
	 * <p>
	 * This method allows you to produce an {@link Index}.
	 * </p>
	 * <p>
	 * When no parameter is passed to the directory static method, it indexes the directory specified in the {@link Config}.
	 * </p>
	 * 
	 * @return
	 */
	public static Index directory() {
		if (preStartup == null)
			preStartup = readMyIndex();
		
		if (instance == null)
			instance = directory(Config.getPath().toFile()); 
		return instance;
	}
	
	/**
	 * <p>This method returns the {@link Index} when the program was started.</p>
	 * 
	 * <p>At the start of the program before the {@link Index} directory is updated, the system first retrieves the old {@link Index} by parsing the available <i>indexlist.txt</i> prior to running.
	 * The values of the old <i>indexlist.txt</i> is returned by this function.</p>
	 * 
	 * <p>This Index will usually be used to compare the pre-Index (this one) and the post-Index (after changes to the directory have been made).</p>
	 * @return
	 */
	public static Index startUp(){
		return preStartup;
	}

	/**
	 * This method can be called when you wish to import the current index list from the file.
	 */
	public static Index readMyIndex() {
		try {
			File file = new File("indexlist.txt");
			return read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * <p>
	 * This method is used to parse the given Index file.
	 * </p>
	 * 
	 * @param indexFile
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static Index read(File indexFile) throws NumberFormatException, IOException {
		Index index = new Index();
		String indexLine = null;
		BufferedReader br = new BufferedReader(new FileReader(indexFile));

		while ((indexLine = br.readLine()) != null) {
			String[] i = indexLine.split(":");
			
			String filename = i[0];
			Long lastModified = Long.parseLong(i[1]);
			
			index.put(filename, lastModified);
		}
		br.close();

		return index;
	}

	/**
	 * <p>
	 * This method is called when you wish to compare the differences between the index lists between a server and a client.
	 * </p>
	 * 
	 * @param server
	 *            The Index file of the server
	 * @param client
	 *            The Index file of the client
	 * @return A HashMap of files and their respective actions.
	 */
	public static FileAndAction compare(Index server, Index client) {
		FileAndAction actions = new FileAndAction();

		for (FileAndLastModifiedPair pair : client) {
			String file = pair.file;
			
			long clientLastModified = client.get(file);
			long serverLastModified = server.get(file);

			if (server.containsFile(file) && serverLastModified > clientLastModified) {
				actions.put(file, "ADD");
			} else {
				actions.put(file, "REQUEST");
			}
		}

		return actions;
	}

	/**
	 * @param file
	 * @return true if the file is in this index; false if otherwise
	 */
	private boolean containsFile(String file) {
		for (FileAndLastModifiedPair pair : this){
			if (pair.file.equalsIgnoreCase(file)) 
				return true;
		}
		return false;
	}

	/**
	 * Given a specific file name, this method returns the date it was last modified. 
	 * @param file
	 * @return the date it was last modified in milliseconds; -1 if deleted or 0 if the file was not found.
	 */
	private long get(String file) {
		for (FileAndLastModifiedPair pair : this){
			if (pair.file.equalsIgnoreCase(file)) 
				return pair.dateModified;
		}
		return 0;
	}

	/**
	 * <p>
	 * This method sorts the list of files in the index by their last modified date. This method used only for the toString() method.
	 * </p>
	 */
	private void sort() {
		Collections.sort(this, new IndexComparator());
	}

	@Override
	public String toString() {
		if (toStringCount == size())
			return sb.toString();

		sb = new StringBuilder();
		sb = sb.append("Index list:\n");

		sort();

		for (FileAndLastModifiedPair pair : this) {
			Long lastModified = pair.dateModified;
			String file = pair.file;

			Date date = new Date(lastModified);
			String format = DateFormat.getInstance().format(date);

			sb.append(format + " - " + file + "\n");
		}

		return sb.toString();
	}

	/**
	 * The {@link IndexComparator} is used to sort the {@link Index} by their date of last modification. It is used in the toString() method.
	 * 
	 * @author Darren
	 *
	 */
	private class IndexComparator implements Comparator<FileAndLastModifiedPair> {
		public int compare(FileAndLastModifiedPair a, FileAndLastModifiedPair b) {
			if (a.dateModified < b.dateModified)
				return -1;
			if (a.dateModified > b.dateModified)
				return 1;
			return 0;
		}

	}

	public void put(String filename, long lastModified) {
		FileAndLastModifiedPair pair = new FileAndLastModifiedPair(filename, lastModified);
		add(pair);
	}

	

}