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
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import message.IndexListPacketHeader;
import dropos.Config;

/**
 * The {@link Index} holds a {@link HashMap} of file names and their date of
 * last modification.
 * 
 * @author Kyle
 *
 */
public class Index extends ArrayList<FileAndLastModifiedPair> {

	public enum FileDifference {
		SAME, MISSING, OUTDATED, UPDATED
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static HashMap<Integer, Index> instance = new HashMap<Integer, Index>();
	private static HashMap<Integer, Index> preStartup = new HashMap<Integer, Index>();

	private StringBuilder sb;
	private int toStringCount = -1;

	private File[] directoryFilePaths;

	public Index() {

	}

	/**
	 * <p>
	 * This method recursively indexes a directory. If it finds a file, it adds
	 * the file to the index. If it finds a folder, it performs the indexing on
	 * the whole directory.
	 * </p>
	 * 
	 * @param directory
	 *            The directory to be indexed
	 * @throws IOException
	 *             This error is thrown when the file is missing, or if there is
	 *             an error.
	 */
	private void indexDirectory(File directory) throws IOException {
		BasicFileAttributes attributes;

		if (Files.notExists(directory.toPath(), LinkOption.NOFOLLOW_LINKS)) {
			Files.createDirectories(directory.toPath());
		}

		directoryFilePaths = directory.listFiles();

		for (File filePath : directoryFilePaths) {

			if (filePath.isDirectory()) {
				// Recursively index the detected folder
				indexDirectory(filePath);
			} else {
				String name = filePath.getName().toString();

				// Ignore OSX related files that start with .
				if (name.startsWith("."))
					continue;

				// Ignore indexlist.txt file
				if (name.equalsIgnoreCase("indexlist.txt"))
					continue;
				
				// Ignore the indexes folder
				if (filePath.isDirectory() && name.equalsIgnoreCase("indexes"))
					continue;

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
	 * This method exports the text file which contains the index list. It uses
	 * the indexes detected by the indexServerDirectory method.
	 * </p>
	 * 
	 * @return The file reference to the index list
	 */
	public File write(int port) {
		Path instancePath = Config.getInstancePath(port);
		File indexList = new File(instancePath + "\\indexlist.txt");

		try {
			FileOutputStream out = new FileOutputStream(indexList);

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

			for (FileAndLastModifiedPair pair : this) {
				String filename = pair.file;
				Long lastModified = pair.lastModified;
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
	 * This method allows you to produce an {@link Index} of a selected
	 * directory.
	 * </p>
	 * 
	 * <p>
	 * Usually, the parameter passed here is the directory specified in the
	 * {@link Config} class.
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
	 * When no parameter is passed to the directory static method, it indexes
	 * the directory specified in the {@link Config}.
	 * </p>
	 * 
	 * @param port
	 *            the instance value of the port
	 * @return
	 */
	public static synchronized Index directory(int port) {
		if (preStartup.containsKey(port) == false) {
			Index pre = readMyIndex(port);
			preStartup.put(port, pre);
		}
		
		Index now = directory(Config.getInstancePath(port).toFile());
		instance.put(port, now);
		return now;
	}

	public static synchronized Index getInstance(int port) {
		if (instance.containsKey(port))
			return instance.get(port);
		return directory(port);
	}

	/**
	 * <p>
	 * This method returns the {@link Index} when the program was started.
	 * </p>
	 * 
	 * <p>
	 * At the start of the program before the {@link Index} directory is
	 * updated, the system first retrieves the old {@link Index} by parsing the
	 * available <i>indexlist.txt</i> prior to running. The values of the old
	 * <i>indexlist.txt</i> is returned by this function.
	 * </p>
	 * 
	 * <p>
	 * This Index will usually be used to compare the pre-Index (this one) and
	 * the post-Index (after changes to the directory have been made).
	 * </p>
	 * 
	 * @param port
	 * 
	 * @return
	 */
	public static Index startUp(int port) {
		if (preStartup.containsKey(port))
			return preStartup.get(port);
		
		// Perform startUp indexing
		directory(port);
		
		// Do some weird indexing shit
		return startUp(port);
	}

	/**
	 * This method can be called when you wish to import the current index list
	 * from the file.
	 */
	public static Index readMyIndex(int port) {
		try {
			File file = new File(Config.getInstancePath(port) + "\\indexlist.txt");
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


		Path path = indexFile.toPath();
		
		// check if parent exists; create if it doesn't
		Path parent = path.getParent();
		if (Files.notExists(parent))
			Files.createDirectory(parent);
		
		
		// check if indexlist.txt exists; create if it doesn't
		if (Files.notExists(path))
			Files.createFile(path);

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
	 * Read this function as <b> the index calling this method has a/n _____
	 * file </b>.
	 * </p>
	 * 
	 * <p>
	 * e.g. <i>This index has an updated file.</i>
	 * </p>
	 * 
	 * @param pair
	 *            the file pair to be inspected
	 * @return FileDifference values of either 'same', 'outdated', 'updated', or
	 *         'missing'
	 */
	public FileDifference containsPair(FileAndLastModifiedPair pair) {
		for (FileAndLastModifiedPair currentPair : this) {

			if (currentPair.exactlyEquals(pair))
				return FileDifference.SAME;

			if (currentPair.equals(pair)) {
				if (currentPair.lastModified < pair.lastModified)
					return FileDifference.OUTDATED;

				if (currentPair.lastModified > pair.lastModified)
					return FileDifference.UPDATED;
			}
		}
		return FileDifference.MISSING;
	}

	/**
	 * <p>
	 * This method sorts the list of files in the index by their last modified
	 * date. This method used only for the toString() method.
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
		sb = sb.append("\nIndex list:\n");

		sort();

		for (FileAndLastModifiedPair pair : this) {
			Long lastModified = pair.lastModified;
			String file = pair.file;

			Date date = new Date(lastModified);
			String format = DateFormat.getInstance().format(date);

			sb.append(format + " - " + file + "\n");
		}

		return sb.toString();
	}

	/**
	 * The {@link IndexComparator} is used to sort the {@link Index} by their
	 * date of last modification. It is used in the toString() method.
	 * 
	 * @author Darren
	 *
	 */
	private class IndexComparator implements Comparator<FileAndLastModifiedPair> {
		public int compare(FileAndLastModifiedPair a, FileAndLastModifiedPair b) {
			if (a.lastModified < b.lastModified)
				return -1;
			if (a.lastModified > b.lastModified)
				return 1;
			return 0;
		}

	}

	public void put(String filename, long lastModified) {
		FileAndLastModifiedPair pair = new FileAndLastModifiedPair(filename, lastModified);
		add(pair);
	}

	public File getFile() throws IOException {
		File f = new File("indexlist.txt");
		// create indexlist.txt if it does not exist
		if (!f.exists())
			Files.createFile(f.toPath());
		return f;
	}

	/**
	 * <p>
	 * This method returns the packet header for sending the index list over the
	 * network.
	 * </p>
	 * <p>
	 * The format is: <i>INDEX filesize</i>
	 * </p>
	 * <p>
	 * e.g. INDEX 5294
	 * </p>
	 * 
	 * 
	 * @return byte array which contains the packet header
	 */
	public IndexListPacketHeader getPacketHeader(int port) {
		return new IndexListPacketHeader(port);
	}

}
