package org.vanda.studio.model.elements;

public abstract class Tool<F> implements Element {
	public abstract String getDate();
	
	public Element clone() {
		// tools are immutable
		return this;
	}

}
