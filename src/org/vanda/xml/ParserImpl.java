package org.vanda.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.vanda.util.Observer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ParserImpl<T> implements Parser<T> {

	private final Observer<T> o;
	private XmlPullParser xp;
	private FileReader reader;
	private ElementHandler rootHandler;

	public ParserImpl(Observer<T> o) {
		this.o = o;
	}

	public void setRootState(ElementHandler rootHandler) {
		this.rootHandler = rootHandler;
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
		if (rootHandler == null)
			return;
		LinkedList<ElementHandler> stack = new LinkedList<ElementHandler>();
		ElementHandler current = null;
		try {
			int eventType = xp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					if (!stack.isEmpty())
						throw fail(null);
					current = rootHandler;
					// stack.push(current);
					current.startElement(null, null);
				} else if (eventType == XmlPullParser.START_TAG) {
					if (current == null) // || stack.size() < 1)
						throw fail(null);
					stack.push(current);
					// System.out.println(xp.getName());
					current = current.handleChild(xp.getNamespace(),
							xp.getName());
					if (current == null)
						throw fail(null);
					current.startElement(xp.getNamespace(), xp.getName());
					int c = xp.getAttributeCount();
					for (int i = 0; i < c; i++)
						current.handleAttribute(xp.getAttributeNamespace(i),
								xp.getAttributeName(i), xp.getAttributeValue(i));
				} else if (eventType == XmlPullParser.END_TAG) {
					if (current == null || stack.size() < 1)
						throw fail(null);
					current.endElement(xp.getNamespace(), xp.getName());
					current = stack.pop();
				} else if (eventType == XmlPullParser.TEXT) {
					if (current == null)
						throw fail(null);
					current.handleText(xp.getText());
				}
				// System.out.println(stack);
				// System.out.println(current);
				eventType = xp.next();
			}
			if (current == null || stack.size() != 0)
				throw fail(null);
			current.endElement(null, null);
		} catch (XmlPullParserException e) {
			throw fail(e);
		} catch (IOException e) {
			throw fail(e);
		}
	}

	@Override
	public Observer<T> getObserver() {
		return o;
	}

	@Override
	public RuntimeException fail(Throwable e) {
		// TODO better exception
		if (e != null) {
			return new RuntimeException("Parsing error", e);
		} else {
			return new RuntimeException("Parsing error");
		}
	}

}