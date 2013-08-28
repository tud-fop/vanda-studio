package org.vanda.view;

public class WorkflowView extends AbstractView {

	@Override
	public void remove(View view) {
		// do nothing
	}

	@Override
	public void visit(SelectionVisitor sv, View view) {
		sv.visitWorkflow(view.getWorkflow());
	}

}
