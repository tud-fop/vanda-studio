package org.vanda.render.jgraph;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class InPortCell extends Cell {
	final String portType; // input or output port

	public InPortCell(Graph graph, LayoutManager layout, Cell parent,
			String portType) {
		
		super(JGraphRendering.inPortRenderer, null, graph, parent);
		this.portType = portType;
	}

	@Override
	public String getType() {
		return portType;
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
	public void onResize(mxGraph graph) {
		// do nothing
	}

	@Override
	public void setSelection(boolean selected) {
		// do nothing
	}
}
