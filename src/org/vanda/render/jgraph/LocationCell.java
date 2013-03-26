package org.vanda.render.jgraph;


import org.vanda.util.Observer;
import org.vanda.view.AbstractView;
import org.vanda.view.View;
import org.vanda.view.AbstractView.ViewEvent;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class LocationCell extends Cell {

	private final Port port;
	private Location variable;
	
	public LocationCell(final Graph g, LayoutManagerInterface layout, Cell parent, Port port, Location variable) {
		this.port = port;
		this.variable = variable;
		
		// Register at LocationView
		g.getView().getLocationView(variable).getObservable().addObserver(new Observer<ViewEvent<AbstractView>> () {

			@Override
			public void notify(ViewEvent<AbstractView> event) {
				getObservable().notify(new SelectionChangedEvent<Cell>(LocationCell.this));
				
			}
		});
		
		// Register at Graph
		getObservable().addObserver(new org.vanda.util.Observer<CellEvent<Cell>> () {

			@Override
			public void notify(CellEvent<Cell> event) {
				event.doNotify(g.getCellChangeListener());
			}
		});
		
		// Create mxCell and add it to Graph
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
		return "LocationCell";
	}

	@Override
	public void onRemove(View view) {		
	}

	@Override
	public void onInsert(final Graph graph, mxICell parent, mxICell cell) {		
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
		return view.getLocationView(variable);
	}

}
