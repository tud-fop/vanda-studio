package org.vanda.workflows.toolinterfaces;

import org.vanda.types.Type;
import org.vanda.workflows.elements.Port;

public class PortBuilder {

	String name;
	Type type;
	
	public void reset() {
		name = "";
		type = null;
	}
	
	public Port build() {
		return new Port(name, type);
	}
	
}
