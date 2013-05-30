package org.vanda.render.jgraph;

import org.vanda.render.jgraph.Cells.CellEvent;
import org.vanda.util.MultiplexObserver;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public abstract class Cell {

	protected MultiplexObserver<CellEvent<Cell>> observable;
	protected mxCell visualization;

	protected Object z;

	// FIXME remove parent
	protected Cell(Renderer r, LayoutManager layoutManager, final Graph graph/*
																			 * ,
																			 * Cell
																			 * parent
																			 */) {
		observable = new MultiplexObserver<CellEvent<Cell>>();
		setZ(r);
		// Register at Graph
		if (graph != null) {
			getObservable().addObserver(
					new org.vanda.util.Observer<CellEvent<Cell>>() {

						@Override
						public void notify(CellEvent<Cell> event) {
							event.doNotify(graph.getCellChangeListener());
						}

					});
		}
		// if (r != null && graph != null && parent != null) {
		// Create mxCell and add it to Graph
		// graph.getGraph().getModel().beginUpdate();
		// try {
		visualization = new mxCell(this);
		visualization.setVisible(true);
		// r.render(graph, this);
		// graph.getGraph().addCell(visualization,
		// parent.getVisualization());
		// } finally {
		// graph.getGraph().getModel().endUpdate();
		// }
		// }
	}

	public void addCell(Cell cell, Object layout) {
		getVisualization().insert(cell.getVisualization());
	}

	public double getHeight() {
		return visualization.getGeometry().getHeight();
	}

	public String getLabel() {
		return "";
	}

	public abstract LayoutSelector getLayoutSelector();

	public MultiplexObserver<CellEvent<Cell>> getObservable() {
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
		getVisualization().remove(cell.getVisualization());
		cell.getVisualization().setParent(null);
	}

	public void setDimensions(double[] dimensions) {
	}

	public abstract void setSelection(boolean selected);

	public void setVisualization(mxCell visualization) {
		this.visualization = visualization;
	}

	public void setZ(Object z) {
		this.z = z;
	}

}
