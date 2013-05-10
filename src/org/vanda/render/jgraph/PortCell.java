package org.vanda.render.jgraph;

import org.vanda.workflows.elements.Port;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class PortCell extends Cell {
	final Port port;
	final String portType; // input or output port
	
	public PortCell(Graph g, LayoutManagerInterface layout, Cell parent, Port port, String portType) {
		this.port = port;
		this.portType = portType;
		
		// Create Cell in Graph
		g.getGraph().getModel().beginUpdate();
		try {
			visualization = new mxCell(this);
			g.getGraph().addCell(visualization, parent.getVisualization());
		} finally {
			g.getGraph().getModel().endUpdate();
		}
		
		// Register at LayoutManager
		layout.register(this);

	}

	@Override
	public String getType() {
		return portType;
	}

	@Override
	public void onRemove() {
		// do nothing
	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {
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
