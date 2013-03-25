package org.vanda.render.jgraph;

import org.vanda.util.MultiplexObserver;
import org.vanda.view.AbstractView;
import org.vanda.view.View;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public abstract class Cell {
	protected Object z;
	protected mxCell visualization;
	protected double[] dimensions = new double[4];
	protected CellObservable observable;
		
	public void setDimensions(double[] dimensions) {
		this.dimensions = dimensions;
	}
	public Object getZ() {
		return z;
	}
	public mxCell getVisualization() {
		return visualization;
	}
	
	public double getX() {
		return dimensions[0];
	}
	public double getY() {
		return dimensions[1];
	}
	public double getWidth() {
		return dimensions[2];
	}
	public double getHeight() {
		return dimensions[3];
	}
	
	public abstract String getType();
	
	public abstract void setSelection(View view); 
	
	public abstract void onRemove(mxICell previous); 
	
	public abstract void onInsert(mxGraph graph);
	
	public abstract boolean inModel();
	
	public abstract void onResize(mxGraph graph); 
	
	public abstract AbstractView getView(View view);
	
	public CellObservable getObservable() {
		return observable;
	}
	
	public class CellObservable extends MultiplexObserver<CellEvent<Cell>> {
		
	}
	/*public class CellObservable implements org.vanda.util.Observable<CellEvent<Cell>> {
		
		protected ArrayList<Observer<? super CellEvent<Cell>>> observers;
		public CellObservable() {
			observers = new ArrayList<Observer<? super CellEvent<Cell>>>();
		}
		@Override
		public void addObserver(Observer<? super CellEvent<Cell>> o) {
			if (o == null)
				throw new IllegalArgumentException("observer must not be null");
			if (!observers.add(o))
				throw new UnsupportedOperationException("cannot add observer twice");
		
			
		}

		@Override
		public void removeObserver(Observer<? super CellEvent<Cell>> o) {
			if (o == null)
				throw new IllegalArgumentException("observer must not be null");
			if (!observers.remove(o))
				throw new UnsupportedOperationException("attempt to remove unregistered observer");
			
		}
		public void notify(CellEvent<Cell> event) {
				for (Observer<? super CellEvent<Cell>> o : observers)
					o.notify(event);
		}
		
	}*/
	
	public static interface CellEvent<C> {
		void doNotify(CellListener<C> cl);
	}
	
	public static interface CellListener<C> {
		void propertyChanged(C c);
		void selectionChanged(C c);
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
	
	public static class SelectionChangedEvent<C> implements CellEvent<C> {
		private final C c;
		
		public SelectionChangedEvent(C c) {
			this.c = c;
		}
		@Override
		public void doNotify(CellListener<C> cl) {
			cl.selectionChanged(c);
		}
		
	}
	
}
