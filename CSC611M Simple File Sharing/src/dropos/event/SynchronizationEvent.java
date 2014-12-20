package dropos.event;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;

public class SynchronizationEvent {
	private Path path;
	private long lastModified;
	private EventType type;

	public enum EventType {
		UPDATE, REQUEST, DELETE
	}
	
	public SynchronizationEvent(Path path, Kind<?> kind) {
		this.path = path;
		if (ENTRY_CREATE == kind) {
			type = EventType.UPDATE;
		} else if (ENTRY_DELETE == kind) {
			type = EventType.DELETE;
		} else if (ENTRY_MODIFY == kind) {
			type = EventType.UPDATE;
		}
		path.toFile();
		// Get the attributes and add an index entry
		BasicFileAttributes attributes;
		try {
			attributes = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			lastModified = attributes.lastModifiedTime().toMillis();
		} catch (IOException e) {
			System.err.println("Could not get last modified date of file " + path.getFileName());
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SynchronizationEvent){
			SynchronizationEvent synchronizationEvent = (SynchronizationEvent) obj; 
			return synchronizationEvent.lastModified == lastModified && synchronizationEvent.path.getFileName().equals(getFileName());
		}
		return super.equals(obj);
	}

	public EventType getType() {
		return type;
	}

	public Path getFileName() {
		// return file;
		return path.getFileName();
	}

	public byte[] getBytes() {
		byte[] rawBytes = null;

		try {
			String message = this.toString();
			rawBytes = message.getBytes("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rawBytes;
	}

	public String toString() {
		long size = 0;
		try {
			size = Files.size(path);
			switch (type) {
			case UPDATE:
				return "UPDATE:" + size + ":" + path.getFileName();
			case DELETE:
				return "DELETE:" + path;
			case REQUEST:
				return "REQUEST:" + path.getFileName();
			}
		} catch (IOException e) {
			System.err.println("UnknownSynchronizatioEventException!");
			e.printStackTrace();
		}
		return "Unknown directory event type!";
	}
}
