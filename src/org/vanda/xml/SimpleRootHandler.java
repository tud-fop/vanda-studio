package org.vanda.xml;

import org.vanda.util.Observer;

public class SimpleRootHandler<T> implements ElementHandler {
	
	private final Parser<T> p;
	private final ElementHandlerFactory<Observer<T>> ehf;
	
	public SimpleRootHandler(Parser<T> p, ElementHandlerFactory<Observer<T>> ehf) {
		this.p = p;
		this.ehf = ehf;
	}

	@Override
	public void startElement(String namespace, String name) {
	}

	@Override
	public void handleAttribute(String namespace, String name, String value) {
	}

	@Override
	public ElementHandler handleChild(String namespace, String name) {
		ElementHandler result = ehf.create(name, p, p.getObserver());
		if (result != null)
			return result;
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
