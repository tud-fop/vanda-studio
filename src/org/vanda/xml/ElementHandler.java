package org.vanda.xml;

public interface ElementHandler {
	void startElement(String namespace, String name);
	void handleAttribute(String namespace, String name, String value);
	ElementHandler handleChild(String namespace, String name);
	void endElement(String namespace, String name);
	void handleText(String text);
}
