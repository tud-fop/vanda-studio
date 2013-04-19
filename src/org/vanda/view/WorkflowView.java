package org.vanda.view;

public class WorkflowView extends AbstractView {

	@Override
	public void remove(View view) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visit(SelectionVisitor sv, View view) {
		sv.visitWorkflow(view.getWorkflow());
	}

}
