package org.vanda.render.jgraph;

import java.util.Observable;
import java.util.Observer;

import org.vanda.view.AbstractView;
import org.vanda.view.View;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class LocationCell extends Cell {

	private final Port port;
	private Location variable;
	
	private class LocationViewObserver implements Observer {
		@Override
		public void update(Observable arg0, Object arg1) {
			getObservable().notify(new SelectionChangedEvent<Cell>(LocationCell.this));
		}
	}
	public LocationCell(final Graph g, LayoutManagerInterface layout, Cell parent, Port port, Location variable) {
		this.port = port;
		this.variable = variable;
		g.getView().getLocationView(variable).addObserver(new LocationViewObserver());
		
		// Register Graph
		getObservable().addObserver(new org.vanda.util.Observer<CellEvent<Cell>> () {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(g.getCellChangeListener());
			}
		});
						
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
		return "LocationCell";
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
		view.getLocationView(variable).setSelected(true);	
	}
	
	public void updateLocation(Job job) {
		variable = job.bindings.get(port);
	}

	@Override
	public AbstractView getView(View view) {
		// TODO Auto-generated method stub
		return null;
	}

}
