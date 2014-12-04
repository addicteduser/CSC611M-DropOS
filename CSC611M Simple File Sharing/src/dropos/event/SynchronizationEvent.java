package dropos.event;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;

public class SynchronizationEvent {
	private Path file;
	private EventType type;

	public enum EventType {
		ADD, REQUEST, DELETE
	}
	
	public SynchronizationEvent(Path file, Kind<?> kind) {
		this.file = file;
		if (ENTRY_CREATE == kind) {
			type = EventType.ADD;
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
		return file;
	}
	
	public byte[] getBytes(){
		byte[] rawBytes = null;
		
		try {
			String message = this.toString();	
			rawBytes = message.getBytes("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rawBytes;
	}
	
	public String toString(){
		switch(type){
		case ADD: 
			return "ADD " + file;
		case DELETE:
			return "DELETE " + file;
		case REQUEST: 
			return "MODIFY " + file;
		}
		return "Unknown directory event type!";
	}
}
