package org.vanda.render.jgraph;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class WorkflowCell extends Cell {
	DataInterface di;
	public WorkflowCell(DataInterface di) {
		super(null, null, null);
		this.di = di;
	}

	@Override
	public String getType() {
		return "WorkflowCell";
	}

	@Override
	public void onInsert(Graph graph, mxICell parent, mxICell cell) {
	}

	@Override
	public void onRemove() {
	}

	@Override
	public void onResize(mxGraph graph) {
	}

	@Override
	public void setSelection(boolean selected) {
		// do nothing
	}

	public DataInterface getDataInterface() {
		return di;
	}

	@Override
	public LayoutSelector getLayoutSelector() {
		return LayoutManager.WORKFLOW;
	}

}
