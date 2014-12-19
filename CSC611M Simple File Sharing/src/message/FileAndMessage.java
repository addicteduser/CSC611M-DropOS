package message;

import java.io.File;

public class FileAndMessage extends Message {
	protected File file;
	public FileAndMessage(String message, File file) {
		super(message);
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	@Override
	public String toString() {
		return message + "\n" + file.getName();
	}
}
