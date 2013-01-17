package org.vanda.workflows.toolinterfaces;

import org.vanda.types.Type;
import org.vanda.workflows.elements.Port;

public class PortBuilder {

	final ToolBuilder parent;
	
	String name;
	Type type;
	
	PortBuilder(ToolBuilder parent) {
		this.parent = parent;
	}
	
	public void reset() {
		name = "";
		type = null;
	}
	
	public Port build() {
		return new Port(name, type);
	}
	
}
