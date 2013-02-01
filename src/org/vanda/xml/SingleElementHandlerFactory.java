package org.vanda.xml;

public interface SingleElementHandlerFactory<Builder> extends
		ElementHandlerFactory<Builder> {
	
	String getTag();

}
