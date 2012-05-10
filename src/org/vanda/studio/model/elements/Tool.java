package org.vanda.studio.model.elements;

public abstract class Tool<F> implements Element {
	public abstract String getAuthor();

	public abstract String getDate();

	public abstract String getDescription();
	
	public Element clone() {
		// tools are immutable
		return this;
	}

}
