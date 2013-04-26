package org.vanda.render.jgraph;

import org.vanda.util.MultiplexObserver;
import org.vanda.view.AbstractView;
import org.vanda.view.View;
import org.vanda.workflows.hyper.Job;

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
	
	public void setVisualization(mxCell visualization) {
		this.visualization = visualization;
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
	
	public abstract void onRemove(View view); 
	
	public abstract void onInsert(final Graph graph, mxICell parent, mxICell cell);
	
	public abstract void onResize(mxGraph graph); 
	
	public abstract AbstractView getView(View view);
	
	public CellObservable getObservable() {
		return observable;
	}
	
	public class CellObservable extends MultiplexObserver<CellEvent<Cell>> {
		
	}
	
	public static interface CellEvent<C> {
		void doNotify(CellListener<C> cl);
	}
	
	public static interface CellListener<C> {
		void propertyChanged(C c);
		void selectionChanged(C c);
		void markChanged(C c);
		void removeCell(C c);
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
	
	public static class CellRemovedEvent<C> implements CellEvent<C> {
		private final C c;
		public CellRemovedEvent(C c) {
			this.c = c;
		}
		@Override
		public void doNotify(CellListener<C> cl) {
			cl.removeCell(c);
		}
		
		
	}

	public String getLabel() {
		return "";
	}
	
	public void updateLocation(Job job) {
		// do Nothing
	}
	
}
