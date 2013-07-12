package org.vanda.render.jgraph;

import org.vanda.render.jgraph.Cells.SetSelectionEvent;

import com.mxgraph.model.mxICell;

public class WorkflowCell extends Cell {
	DataInterface di;

	public WorkflowCell(DataInterface di) {
		super(null, null, null);
		this.di = di;
	}

	public DataInterface getDataInterface() {
		return di;
	}

	@Override
	public LayoutSelector getLayoutSelector() {
		return LayoutManager.WORKFLOW;
	}

	@Override
	public boolean isSelectable() {
		return true;
	}

	@Override
	public boolean isValidConnectionSource() {
		return false;
	}

	@Override
	public boolean isValidConnectionTarget() {
		return false;
	}

	@Override
	public boolean isValidDropTarget() {
		return true;
	}

	@Override
	public void onInsert(Graph graph, mxICell parent, mxICell cell) {
	}

	@Override
	public void onRemove() {
	}

	@Override
	public void onResize(Graph graph) {
		// do nothing
	}

	@Override
	public void setSelection(boolean selected) {
		getObservable().notify(new SetSelectionEvent<Cell>(this, selected));
	}

}
