package org.vanda.studio.model.hyper;

import java.util.Arrays;
import java.util.List;

import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.generation.WorkflowElement;
import org.vanda.studio.model.workflows.Job;
import org.vanda.studio.model.workflows.RendererAssortment;
import org.vanda.studio.util.HasActions;

public abstract class HyperJob<V> implements WorkflowElement, HasActions, Cloneable {
	
	protected double[] dimensions = new double[4];
	
	protected HyperWorkflow<?, V> parent;
	
	@Override
	public HyperJob<V> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked")
		HyperJob<V> cl = (HyperJob<V>) super.clone();
		cl.dimensions = Arrays.copyOf(dimensions, 4);
		return cl;
	}
	
	public double getHeight() {
		return dimensions[3];
	}

	@Override
	public abstract List<Port> getInputPorts();

	public abstract String getName();
	
	public HyperWorkflow<?, V> getParent() {
		return parent;
	}

	@Override
	public abstract List<Port> getOutputPorts();
	
	public abstract Class<V> getViewType();

	public double getWidth() {
		return dimensions[2];
	}

	public double getX() {
		return dimensions[0];
	}

	public double getY() {
		return dimensions[1];
	}

	public abstract boolean isInputPort();

	public abstract boolean isOutputPort();

	public abstract List<Job<V>> unfold();

	public abstract <R> R selectRenderer(RendererAssortment<R> ra);
	
	/** { x, y, width, height } */
	public void setDimensions(double[] d) {
		assert(d.length == 4);
		System.arraycopy(d, 0, dimensions, 0, 4);
	}
	
	public void setParent(HyperWorkflow<?, V> parent) {
		this.parent = parent;
	}

}
