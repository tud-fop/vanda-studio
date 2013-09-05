package org.vanda.view;

import org.vanda.workflows.hyper.ConnectionKey;

public class ConnectionView extends AbstractView {

	public ConnectionView() {
		super();
		//TODO remove this constructor as it is just for debugging
//		System.out.println("Created ConnectionView" + this);
	}
	
	@Override
	public void remove(View view) {
		for (ConnectionKey ck : view.getWorkflow().getConnections())
			if (view.getConnectionView(ck) == this) {
				view.getWorkflow().removeConnection(ck);
				break;
			}
	}

	@Override
	public void visit(SelectionVisitor sv, View view) {
		for (ConnectionKey ck : view.getWorkflow().getConnections())
			if (view.getConnectionView(ck) == this) {
				sv.visitConnection(view.getWorkflow(), ck);
				break;
			}
	}
	
	@Override
	public void finalize() {
		//TODO remove this finalizer as it is just for debugging
//		System.out.println("removed ConnectionView" + this);
	}
}
