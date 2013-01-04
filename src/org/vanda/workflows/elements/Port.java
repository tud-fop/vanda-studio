package org.vanda.workflows.elements;

import org.vanda.types.Type;

/**
 * Port of a Tool. Currently, the type is just represented as a String.
 * Ultimately, one might want to model types using Java classes.
 * 
 * @author buechse
 * 
 */

public class Port {

	String identifier;

	Type type;

	public Port(String identifier, Type type) {
		if (identifier == null)
			this.identifier = toString();
		else
			this.identifier = identifier;
		this.type = type;
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
