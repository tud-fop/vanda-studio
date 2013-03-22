package org.vanda.render.jgraph;

import org.vanda.view.AbstractView;
import org.vanda.view.View;
import org.vanda.workflows.elements.Port;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class PortCell extends Cell {
	final Port port;
	
	public PortCell(Graph g, LayoutManager layout, Cell parent, Port port) {
		this.port = port;
		g.getGraph().getModel().beginUpdate();
		try {
			visualization = new mxCell(this, layout.getGeometry(this), layout.getStyleName(this));
			visualization.setVertex(true);
			g.getGraph().addCell(visualization, parent.getVisualization());
		} finally {
			g.getGraph().getModel().endUpdate();
		}

	}

	@Override
	public String getType() {
		return "PortCell";
	}

	@Override
	public void onRemove(mxICell previous) {
	}

	@Override
	public void onInsert(mxGraph graph) {
	}

	@Override
	public boolean inModel() {
		return false;
	}

	@Override
	public void onResize(mxGraph graph) {

	}

	@Override
	public void setSelection(View view) {		
	}

	@Override
	public AbstractView getView(View view) {
		return null;
	}

	public Port getPort() {
		return port;
	}
}
