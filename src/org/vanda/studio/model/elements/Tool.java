package org.vanda.studio.model.elements;

import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Pair;

public abstract class Tool implements Element {
	public Element clone() {
		// tools are immutable
		return this;
	}
	
	@Override
	public Observable<Element> getNameChangeObservable() {
		return null;
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		v.visitTool(this);
	}

	@Override
	public final Observable<Pair<Element, Integer>> getAddInputPortObservable() {
		return null;
	}

	@Override
	public final Observable<Pair<Element, Integer>> getAddOutputPortObservable() {
		return null;
	}

	@Override
	public final Observable<Pair<Element, Integer>> getRemoveInputPortObservable() {
		return null;
	}

	@Override
	public final Observable<Pair<Element, Integer>> getRemoveOutputPortObservable() {
		return null;
	}

}
