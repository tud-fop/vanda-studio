package org.vanda.xml;

public class RootRootHandler implements ElementHandler {
	
	private final Parser<?> p;
	private String tag;
	private final ElementHandler eh;
	
	public RootRootHandler(Parser<?> p, String tag, ElementHandler eh) {
		this.p = p;
		this.tag = tag;
		this.eh = eh;
	}

	@Override
	public void startElement(String namespace, String name) {
	}

	@Override
	public void handleAttribute(String namespace, String name, String value) {
	}

	@Override
	public ElementHandler handleChild(String namespace, String name) {
		if (name.equals(tag))
			return eh;
		else
			throw p.fail(null);
	}

	@Override
	public void endElement(String namespace, String name) {
	}

	@Override
	public void handleText(String text) {
	}

}
