package org.vanda.render.jgraph;

import com.mxgraph.model.mxICell;

public class InPortCell extends Cell {
	final String portType; // input or output port

	public InPortCell(Graph graph, LayoutManager layout, Cell parent,
			String portType) {
		
		super(JGraphRendering.inPortRenderer, null, graph);
		this.portType = portType;
	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {
		// do nothing
	}

	@Override
	public void onRemove() {
		// do nothing
	}

	@Override
	public void onResize(Graph graph) {
		// do nothing
	}

	@Override
	public void setSelection(boolean selected) {
		// do nothing
	}

	@Override
	public LayoutSelector getLayoutSelector() {
		return LayoutManager.INPORT;
	}

	@Override
	public boolean isSelectable() {
		return false;
	}

	@Override
	public boolean isValidConnectionSource() {
		return false;
	}

	@Override
	public boolean isValidConnectionTarget() {
		return true;
	}

	@Override
	public boolean isValidDropTarget() {
		return false;
	}
}
