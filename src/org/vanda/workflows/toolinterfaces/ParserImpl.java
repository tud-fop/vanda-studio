package org.vanda.workflows.toolinterfaces;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.vanda.util.Observer;
import org.vanda.workflows.elements.ToolInterface;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ParserImpl implements Parser {

	private final Observer<ToolInterface> o;
	private ToolBuilder tb;
	private ToolInterfaceBuilder tib;
	private XmlPullParser xp;
	private FileReader reader;

	public ParserImpl(Observer<ToolInterface> o) {
		this.o = o;
		tb = new ToolBuilder();
		tib = new ToolInterfaceBuilder();
	}

	public void init(File file) throws Exception {
		Exception ex = null;
		if (xp == null) {
			try {
				xp = (XmlPullParser) Class.forName("org.xmlpull.mxp1.MXParser",
						true, XmlPullParser.class.getClassLoader())
						.newInstance();
			} catch (InstantiationException e) {
				ex = e;
			} catch (IllegalAccessException e) {
				ex = e;
			} catch (ClassNotFoundException e) {
				ex = e;
			}
			if (ex != null)
				throw ex;
		}
		reader = new FileReader(file);
		xp.setInput(reader);
		tb.reset();
		tib.reset();
	}

	public void done() {
		try {
			xp.setInput(null);
			reader.close();
		} catch (XmlPullParserException e) {
			// ignore
		} catch (IOException e) {
			// ignore
		}
	}

	public void process() {
		o.notify(null);
	}

	@Override
	public ToolBuilder getToolBuilder() {
		return tb;
	}

	@Override
	public ToolInterfaceBuilder getToolInterfaceBuilder() {
		return tib;
	}

	@Override
	public void buildTool() {
		tib.tools.add(tb);
		tb = new ToolBuilder();
	}

	@Override
	public void buildToolInterface() {
		ToolInterface ti = tib.build();
		o.notify(ti);
		tib.reset();
		tb.reset();
	}

	@Override
	public XmlPullParser getXmlParser() {
		return xp;
	}

}