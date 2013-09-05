package org.vanda.view;

import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;

public class LocationView extends AbstractView {

	@Override
	public void remove(View view) {
		// do nothing
	}

	@Override
	public void visit(SelectionVisitor sv, View view) {
		for (Job j : view.getWorkflow().getChildren())
			for (Port op : j.getElement().getOutputPorts()) {
				Location l = j.bindings.get(op);
				if (view.getLocationView(l) == this) {
					sv.visitVariable(l, view.getWorkflow());
					return;
				}
			}
	}

}
