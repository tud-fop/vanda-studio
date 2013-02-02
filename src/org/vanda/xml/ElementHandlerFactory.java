package org.vanda.xml;

public interface ElementHandlerFactory<Builder1> {
	
	ElementHandler create(String name, Parser<?> p, Builder1 b);

}
