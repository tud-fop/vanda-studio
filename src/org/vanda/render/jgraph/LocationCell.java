package org.vanda.render.jgraph;

import org.vanda.render.jgraph.Cells.SetSelectionEvent;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class LocationCell extends Cell {

	public LocationCell(final Graph graph, LayoutManager layout, Cell parent) {

		super(JGraphRendering.locationRenderer, layout, graph);
	}

	@Override
	public String getType() {
		return "LocationCell";
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
		getObservable().notify(new SetSelectionEvent<Cell>(this, selected));
	}

	@Override
	public LayoutSelector getLayoutSelector() {
		return LayoutManager.LOCATION;
	}

}
