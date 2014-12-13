package message;

public class Message {
	public final String message;
	public Message(final String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return message;
	}
}
