package org.vanda.render.jgraph;

import org.vanda.render.jgraph.Cells.CellObservable;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public abstract class Cell {

	protected double[] dimensions = new double[4];
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
		return dimensions[3];
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

	// public abstract AbstractView getView(View view);

	public abstract String getType();

	public mxCell getVisualization() {
		return visualization;
	}

	public double getWidth() {
		return dimensions[2];
	}

	public double getX() {
		return dimensions[0];
	}

	public double getY() {
		return dimensions[1];
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
		this.dimensions = dimensions;
	}

	public abstract void setSelection(boolean selected);

	public void setVisualization(mxCell visualization) {
		this.visualization = visualization;
	}

	public void setZ(int z) {
		this.z = z;
	}

}
