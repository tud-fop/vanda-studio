package org.vanda.xml;

public interface ElementHandlerFactory<Builder> {
	
	ElementHandler create(String name, Parser<?> p, Builder b);

}
