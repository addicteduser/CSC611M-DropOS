package dropos.event;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

import dropos.Config;

public class SynchronizationEvent {
	private Path file;
	private EventType type;

	public enum EventType {
		UPDATE, REQUEST, DELETE
	}
	
	public SynchronizationEvent(Path file, String action){
		this.file = file;
		
		if (action.equalsIgnoreCase("UPDATE")){
			type = EventType.UPDATE;
		} else if (action.equalsIgnoreCase("DELETE")){
			type = EventType.DELETE;
		} else if (action.equalsIgnoreCase("REQUEST")) {
			type = EventType.REQUEST;
		}
	}
	
	public SynchronizationEvent(Path file, Kind<?> kind) {
		this.file = file;
		System.out.println("CREATE SYNC PATH TO STRING: "+file.toString());
		if (ENTRY_CREATE == kind) {
			type = EventType.UPDATE;
		} else if (ENTRY_DELETE == kind) {
			type = EventType.DELETE;
		} else if (ENTRY_MODIFY == kind) {
			type = EventType.REQUEST;
		}
	}

	public EventType getType() {
		return type;
	}

	public Path getFile() {
		// return file;
		return file.getFileName();
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
			size = Files.size(file);
			switch (type) {
			case UPDATE:
				return "UPDATE " + size + " " + file.getFileName();
			case DELETE:
				return "DELETE " + file;
			case REQUEST:
				return "REQUEST " + file;
			}
		} catch (IOException e) {
			System.err.println("UnknownSynchronizatioEventException!");
			e.printStackTrace();
		}
		return "Unknown directory event type!";
	}
}
