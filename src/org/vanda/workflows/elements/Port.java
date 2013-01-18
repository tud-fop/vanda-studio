package org.vanda.workflows.elements;

import org.vanda.types.Type;

public class Port {

	private final String identifier;

	private Type type;

	public Port(String identifier, Type type) {
		if (identifier == null)
			this.identifier = toString();
		else
			this.identifier = identifier;
		this.type = type;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Port
				&& identifier.equals(((Port) other).identifier);
	}
	
	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	public String getIdentifier() {
		return identifier;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
