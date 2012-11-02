package org.vanda.studio.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import org.vanda.studio.app.Application;

public final class ExceptionMessage implements Message {

	private final Date d;
	private final Throwable e;

	public ExceptionMessage(Throwable e) {
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

	@Override
	public void onSelect(Application app) {
		// TODO invoke change in mxGraph

	}

	@Override
	public void onDeselect(Application app) {
		// TODO invoke change in mxGraph

	}

}
