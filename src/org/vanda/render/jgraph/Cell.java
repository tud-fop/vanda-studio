package org.vanda.render.jgraph;

import java.util.Observable;

import org.vanda.view.AbstractView;
import org.vanda.view.View;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public abstract class Cell extends Observable{
	protected Object z;
	protected mxCell visualization;
	protected double[] dimensions = new double[4];
		
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
	
}
