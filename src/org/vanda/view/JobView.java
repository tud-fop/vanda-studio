package org.vanda.view;

import org.vanda.workflows.hyper.Job;

public class JobView extends AbstractView {

	@Override
	public void remove(View view) {
		for (Job j : view.jobs.keySet())
			if (view.getJobView(j) == this) {
				view.getWorkflow().removeChild(j);
				break;
			}
	}

	@Override
	public void visit(SelectionVisitor sv, View view) {
		for (Job j : view.jobs.keySet())
			if (view.getJobView(j) == this) {
				sv.visitJob(view.getWorkflow(), j);
				break;
			}
		
	}
	
}
