package org.vanda.workflows.serialization;

import org.vanda.types.Type;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.hyper.ElementAdapter;
import org.vanda.workflows.hyper.LiteralAdapter;

public class LiteralBuilder {
	
	public Type type;
	
	public String value;
	
	public ElementAdapter build() {
		return new LiteralAdapter(new Literal(type, value));
	}

}
