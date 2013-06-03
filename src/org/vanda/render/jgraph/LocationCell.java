package org.vanda.render.jgraph;

import org.vanda.render.jgraph.Cells.SetSelectionEvent;
import com.mxgraph.model.mxICell;

public class LocationCell extends Cell {

	public LocationCell(final Graph graph, LayoutManager layout, Cell parent) {

		super(JGraphRendering.locationRenderer, layout, graph);
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
		getObservable().notify(new SetSelectionEvent<Cell>(this, selected));
	}

	@Override
	public LayoutSelector getLayoutSelector() {
		return LayoutManager.LOCATION;
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
		return false;
	}

}
