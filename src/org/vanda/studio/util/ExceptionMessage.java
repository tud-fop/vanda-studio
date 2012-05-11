package org.vanda.studio.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public final class ExceptionMessage implements Message {
	
	private final Exception e;
	
	public ExceptionMessage(Exception e) {
		this.e = e;
	}

	@Override
	public void appendActions(List<Action> as) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getHeadline() {
		if (e.getMessage() != null)
			return e.getMessage();
		else
			return "an unspecified error occurred";
	}

	@Override
	public String getMessage() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

}
