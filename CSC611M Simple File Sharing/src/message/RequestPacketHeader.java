package message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import dropos.Config;

public class RequestPacketHeader extends FilePacketHeader {
	
	public RequestPacketHeader(String header) throws IOException {
		super(header);
	}

}
