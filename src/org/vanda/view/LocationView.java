package org.vanda.view;

import org.vanda.workflows.hyper.Location;

public class LocationView extends AbstractView {

	@Override
	public void remove(View view) {
		// do nothing
	}

	@Override
	public void visit(SelectionVisitor sv, View view) {
		for (Location l : view.variables.keySet())
			if (view.getLocationView(l) == this) { 
				sv.visitVariable(l, view.getWorkflow());
				return;
			}
	}

}