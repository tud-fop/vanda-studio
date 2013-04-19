package org.vanda.render.jgraph;

import org.vanda.presentationmodel.PresentationModel;
import org.vanda.view.AbstractView;
import org.vanda.view.View;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class WorkflowCell extends Cell {
	PresentationModel pm;
	public WorkflowCell(PresentationModel pm) {
		this.pm = pm;
	}
	
	public PresentationModel getPresentationModel() {
		return pm;
	}

	@Override
	public String getType() {
		return "WorkflowCell";
	}

	@Override
	public void setSelection(View view) {
	}

	@Override
	public void onRemove(View view) {
	}

	@Override
	public void onInsert(Graph graph, mxICell parent, mxICell cell) {
	}

	@Override
	public void onResize(mxGraph graph) {
	}

	@Override
	public AbstractView getView(View view) {
		return view.getWorkflowView();
	}

}
