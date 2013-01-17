package org.vanda.workflows.elements;

import org.vanda.util.Observable;

public abstract class Tool implements Element {
	public Element clone() {
		// tools are immutable
		return this;
	}
	
	public abstract String getStatus();
	
	public abstract ToolInterface getInterface();
	
	@Override
	public final Observable<ElementEvent> getObservable() {
		return null;
	}

	@Override
	public void visit(ElementVisitor v) {
		v.visitTool(this);
	}
}
