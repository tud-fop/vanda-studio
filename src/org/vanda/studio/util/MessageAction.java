package org.vanda.studio.util;

public final class MessageAction implements Action {
	
	private final String message;
	
	public MessageAction(Exception e) {
		this(e.getMessage());
	}

	public MessageAction(String message) {
		if (message == null)
			this.message = "an unspecified error occurred";
		else
			this.message = message;
	}

	@Override
	public String getName() {
		return message;
	}

	@Override
	public void invoke() {
		// do nothing
	}

}
