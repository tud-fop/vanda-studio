package org.vanda.render.jgraph;

import java.awt.event.MouseEvent;

import org.vanda.render.jgraph.Cells.CellEvent;
import org.vanda.render.jgraph.Cells.RightClickEvent;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;

public abstract class Cell {

	private MultiplexObserver<CellEvent<Cell>> observable;
	protected Observer<CellEvent<Cell>> graphObserver;
	protected mxCell visualization;

	protected Object z;

	protected Cell(Renderer r, LayoutManager layoutManager, final Graph graph) {
		
		observable = new MultiplexObserver<CellEvent<Cell>>();
		setZ(r);
		
		// Register at Graph
		if (graph != null) {
			graphObserver = new org.vanda.util.Observer<CellEvent<Cell>>() {

				@Override
				public void notify(CellEvent<Cell> event) {
					event.doNotify(graph.getCellChangeListener());
				}

			};
			getObservable().addObserver(graphObserver);
		}

		visualization = new mxCell(this);
		if (r != null)
			r.render(this);
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

	public abstract boolean isSelectable();

	public abstract boolean isValidConnectionSource();

	public abstract boolean isValidConnectionTarget();

	public abstract boolean isValidDropTarget();

	public abstract void onInsert(final Graph graph, mxICell parent,
			mxICell cell);

	public abstract void onRemove();

	public abstract void onResize(Graph graph);

	public void removeCell(Cell cell) {
		getVisualization().remove(cell.getVisualization());
		cell.getVisualization().setParent(null);
	}
	
	public void setDimensions(double[] dimensions) {
	}
	
	public void setId(String id) {
		getVisualization().setId(id);
	}
	
	public abstract void setSelection(boolean selected);
	public void setVisualization(mxCell visualization) {
		this.visualization = visualization;
	}
	public void setZ(Object z) {
		this.z = z;
	}
	
	public void rightMouseClick(MouseEvent e) {
		getObservable().notify(new RightClickEvent<Cell>(e));
	}
	
	public int getChildCount() {
		return visualization.getChildCount();
	}
	
	public Cell getChildAt(int i) {
		return (Cell) visualization.getChildAt(i).getValue();
 	}
	
	public boolean isFoldable() {
		return false;
	}
	
}
