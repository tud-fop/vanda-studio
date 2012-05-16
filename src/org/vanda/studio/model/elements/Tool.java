package org.vanda.studio.model.elements;

public abstract class Tool<F> implements Element {
	public Element clone() {
		// tools are immutable
		return this;
	}

}
