package org.vanda.render.jgraph;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observer;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public abstract class Cell {
	public static interface CellEvent<C> {
		void doNotify(CellListener<C> cl);
	}

	public static interface CellListener<C> {
		void insertCell(C c);

		void markChanged(C c);

		void propertyChanged(C c);

		void removeCell(C c);

		void selectionChanged(C c, boolean selected);

		void setSelection(C c, boolean selected);
	}

	public class CellObservable extends MultiplexObserver<CellEvent<Cell>> {
		int observers = 0;
		@Override
		public void addObserver(Observer<? super CellEvent<Cell>> o) {
			observers++;
			super.addObserver(o);
		}
		
		@Override
		public void removeObserver(Observer<? super CellEvent<Cell>> o) {
			observers--;
			super.removeObserver(o);
		}
		
		public int getObserverCount() {
			return observers;
		}
	}

	public static class InsertCellEvent<C> implements CellEvent<C> {
		private final C c;

		public InsertCellEvent(C c) {
			this.c = c;
		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.insertCell(c);
		}

	}

	public static class MarkChangedEvent<C> implements CellEvent<C> {
		private final C c;

		public MarkChangedEvent(C c) {
			this.c = c;
		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.markChanged(c);
		}

	}

	public static class PropertyChangedEvent<C> implements CellEvent<C> {
		private final C c;

		public PropertyChangedEvent(C c) {
			this.c = c;
		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.propertyChanged(c);
		}
	}

	public static class RemoveCellEvent<C> implements CellEvent<C> {
		private final C c;

		public RemoveCellEvent(C c) {
			this.c = c;
		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.removeCell(c);
		}

	}

	public static class SelectionChangedEvent<C> implements CellEvent<C> {
		private final C c;
		boolean selected;

		public SelectionChangedEvent(C c, boolean selected) {
			this.c = c;
			this.selected = selected;

		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.selectionChanged(c, selected);
		}

	}

	public static class SetSelectionEvent<C> implements CellEvent<C> {
		private final C c;
		private final boolean selected;

		public SetSelectionEvent(C c, boolean selected) {
			this.c = c;
			this.selected = selected;
		}

		@Override
		public void doNotify(CellListener<C> cl) {
			cl.setSelection(c, selected);
		}

	}

	protected double[] dimensions = new double[4];
	protected CellObservable observable;
	protected mxCell visualization;

	protected Object z;

	public double getHeight() {
		return dimensions[3];
	}

	public String getLabel() {
		return "";
	}

	public CellObservable getObservable() {
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
