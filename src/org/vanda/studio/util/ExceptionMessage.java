package org.vanda.studio.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

public final class ExceptionMessage implements Message {

	private final Date d;
	private final Exception e;

	public ExceptionMessage(Exception e) {
		this.d = new Date();
		this.e = e;
		e.printStackTrace();
	}

	@Override
	public void appendActions(List<Action> as) {

	}

	@Override
	public String getHeadline() {
		if (e.getMessage() != null)
			return "Error: " + e.getMessage();
		else
			return "Error: an unspecified error of type "
					+ e.getClass().getSimpleName() + " occurred";
	}

	@Override
	public String getMessage() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	@Override
	public Date getDate() {
		return d;
	}

}
