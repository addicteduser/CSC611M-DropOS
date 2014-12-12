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
import java.util.Set;

public class Index extends HashMap<String, Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<FileAndLastModifiedPair> list = new ArrayList<Index.FileAndLastModifiedPair>();

	private StringBuilder sb;
	private int toStringCount = -1;

	private File[] directoryFilePaths;
	private ArrayList<String> entries = new ArrayList<String>();
	

	public Index(){
		
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
		String indexEntry;
		directoryFilePaths = directory.listFiles();

		for (File filePath : directoryFilePaths) {

			if (filePath.isDirectory()) {
				// Recursively index the detected folder
				indexDirectory(filePath);
			} else {

				// Get the attributes and add an index entry
				attributes = Files.readAttributes(filePath.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
				indexEntry = filePath.getName() + ":" + attributes.lastModifiedTime().toMillis();
				entries.add(indexEntry);
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

			for (String s : entries) {
				writer.write(s + "\n");
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return indexList;
	}
	
	public static Index indexThisDirectory(File directory) {
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
	 * This method can be called when you wish to import the current index list from the file.
	 */
	public static Index getCurrentIndex(){
	try {
		File file = new File("indexlist.txt");
		return read(file);
	}catch(Exception e) {
		e.printStackTrace();
	}
	return null;
	}

	/**
	 * <p>
	 * This method is used to parse the given Index file.
	 * </p>
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
			index.put(i[0], Long.parseLong(i[1]));
		}
		br.close();
		
		return index;
	}

	/**
	 * <p>
	 * This method is called when you wish to compare the differences between the index lists between a server and a client. 
	 * </p>
	 * @param server The Index file of the server
	 * @param client The Index file of the client
	 * @return A HashMap of files and their respective actions.
	 */
	public static FileAndAction compare(Index server, Index client) {
		FileAndAction actions = new FileAndAction();

		Set<String> files = client.keySet();
		for (String file : files) {
			long clientLastModified = client.get(file);
			long serverLastModified = server.get(file);

			if (server.containsKey(file) && serverLastModified > clientLastModified) {
				actions.put(file, "ADD");
			} else {
				actions.put(file, "REQUEST");
			}
		}

		return actions;
	}

	/**
	 * <p>This method sorts the list of files in the index by their last modified date. This method used only for the toString() method.</p>
	 */
	private void sort() {
		list.clear();
		for (String file : keySet()) {
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

		for (FileAndLastModifiedPair pair : list) {
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

	private class FileAndLastModifiedPair {
		String file;
		Long dateModified;

		public FileAndLastModifiedPair(String file, Long dateModified) {
			this.file = file;
			this.dateModified = dateModified;
		}
	}

}