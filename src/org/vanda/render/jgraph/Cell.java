package org.vanda.render.jgraph;

import org.vanda.render.jgraph.Cells.CellObservable;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public abstract class Cell {

	protected CellObservable<Cell> observable;
	protected mxCell visualization;

	protected Object z;

	// TODO implement me
	// protected Cell(Renderer r, LayoutManager layoutManager) {

	// }

	public void addCell(Cell cell, Object layout) {
		// TODO implement me
	}

	public double getHeight() {
		return visualization.getGeometry().getHeight();
	}

	public String getLabel() {
		return "";
	}

	public CellObservable<Cell> getObservable() {
		return observable;
	}

	public Cell getParentCell() {
		return (Cell) getVisualization().getParent().getValue();
	}

	public abstract String getType();

	public mxCell getVisualization() {
		return visualization;
	}

	public double getWidth() {
		return visualization.getGeometry().getWidth();
	}

	public double getX() {
		return visualization.getGeometry().getX();
	}

	public double getY() {
		return visualization.getGeometry().getY();
	}

	public Object getZ() {
		return z;
	}

	public void highlight(boolean highlight) {
		// do nothing
	}

	public abstract void onInsert(final Graph graph, mxICell parent,
			mxICell cell);

	public abstract void onRemove();

	public abstract void onResize(mxGraph graph);

	public void removeCell(Cell cell) {
		// TODO implement me
	}

	public void setDimensions(double[] dimensions) {
	}

	public abstract void setSelection(boolean selected);

	public void setVisualization(mxCell visualization) {
		this.visualization = visualization;
	}

	public void setZ(int z) {
		this.z = z;
	}

}
