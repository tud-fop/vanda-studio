package org.vanda.render.jgraph;

import java.util.Map;

import org.vanda.view.AbstractView;
import org.vanda.view.View;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class PortCell extends Cell {
	final Port port;
	final String portType; // input or output port
	
	public PortCell(Graph g, LayoutManagerInterface layout, Cell parent, Port port, String portType) {
		this.port = port;
		this.portType = portType;		
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
		return portType;
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
