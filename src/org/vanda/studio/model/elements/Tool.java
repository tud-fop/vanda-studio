package org.vanda.studio.model.elements;

import org.vanda.studio.util.Observable;

public abstract class Tool<F> implements Element {
	public Element clone() {
		// tools are immutable
		return this;
	}
	
	@Override
	public Observable<Element> getNameChangeObservable() {
		return null;
	}

	@Override
	public Observable<Element> getPortsChangeObservable() {
		return null;
	}

}
