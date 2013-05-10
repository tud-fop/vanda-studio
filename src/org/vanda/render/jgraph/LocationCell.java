package org.vanda.render.jgraph;


import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class LocationCell extends Cell {

	public LocationCell(final Graph g, LayoutManagerInterface layout, Cell parent) {

		this.observable = new CellObservable();

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
		getObservable().notify(new SetSelectionEvent<Cell>(this, selected));
	}
	
}