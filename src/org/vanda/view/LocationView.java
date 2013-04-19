package org.vanda.view;

import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;

public class LocationView extends AbstractView {

	@Override
	public void remove(View view) {
		// do nothing
	}

	@Override
	public void visit(SelectionVisitor sv, View view) {
		for (Port p : view.variables.keySet())
			if (view.getLocationView(p) == this) { 
				for (Job j : view.jobs.keySet()) 
					if (j.bindings.containsKey(p)) {
						sv.visitVariable(j.bindings.get(p), view.getWorkflow());
						return;
					}
				return;
			}
							
			
		
	}

}
