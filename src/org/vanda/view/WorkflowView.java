package org.vanda.view;

import org.vanda.view.Views.*;
import org.vanda.workflows.hyper.MutableWorkflow;

public class WorkflowView extends AbstractView<MutableWorkflow> {

	public WorkflowView(ViewListener<AbstractView<?>> listener) {
		super(listener);
	}

	@Override
	public SelectionObject createSelectionObject(MutableWorkflow t) {
		return new WorkflowSelectionObject(t);
	}
	
	@Override
	public void setSelected(boolean s) {
		super.setSelected(s);
	}

}
