package org.vanda.workflows.serialization;

import org.vanda.types.Type;
import org.vanda.workflows.elements.Literal;

public class LiteralBuilder {
	
	public Type type;
	
	public String value;
	
	public Literal build() {
		return new Literal(type, value);
	}

}
