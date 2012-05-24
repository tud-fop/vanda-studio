package org.vanda.studio.model.elements;

import org.vanda.studio.util.Observable;

public abstract class Tool implements Element {
	public Element clone() {
		// tools are immutable
		return this;
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		v.visitTool(this);
	}

	@Override
	public final Observable<ElementEvent> getObservable() {
		return null;
	}

}
