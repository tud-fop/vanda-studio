package org.vanda.render.jgraph;

import org.vanda.presentationmodel.PresentationModel;
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
	public void setSelection(boolean selected) {
		// do nothing
	}

	@Override
	public void onRemove() {
	}

	@Override
	public void onInsert(Graph graph, mxICell parent, mxICell cell) {
	}

	@Override
	public void onResize(mxGraph graph) {
	}

}
