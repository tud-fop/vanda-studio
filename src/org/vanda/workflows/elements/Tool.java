package org.vanda.workflows.elements;

import java.util.Collections;
import java.util.Set;

import org.vanda.util.Observable;

public abstract class Tool implements Element {
	public Element clone() {
		// tools are immutable
		return this;
	}
	
	public Set<String> getImports() {
		return Collections.emptySet();
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
