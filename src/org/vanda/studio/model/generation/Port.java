package org.vanda.studio.model.generation;

/**
 * Port of a Tool. Currently, the type is just represented as a String.
 * Ultimately, one might want to model types using Java classes.
 * 
 * @author buechse
 * 
 */

public class Port {

	String identifier;

	String type;

	public Port(String identifier, String type) {
		if (identifier == null)
			this.identifier = toString();
		else
			this.identifier = identifier;
		this.type = type;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getType() {
		return type;
	}
}
