package org.vanda.view;

import org.vanda.workflows.hyper.ConnectionKey;

public class ConnectionView extends AbstractView {

	@Override
	public void remove(View view) {
		System.out.println("Remove in ConnectionView");
		for (ConnectionKey ck : view.connections.keySet())
			if (view.getConnectionView(ck) == this) {
				view.getWorkflow().removeConnection(ck);
				break;
			}
	}

	@Override
	public void visit(SelectionVisitor sv, View view) {
		for (ConnectionKey ck : view.connections.keySet())
			if (view.getConnectionView(ck) == this) {
				sv.visitConnection(view.getWorkflow(), ck);
				break;
			}
	}
	
}
