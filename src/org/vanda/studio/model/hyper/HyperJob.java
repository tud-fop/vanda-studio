package org.vanda.studio.model.hyper;

import java.util.Arrays;
import java.util.List;

import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.generation.WorkflowElement;
import org.vanda.studio.model.workflows.RendererAssortment;
import org.vanda.studio.util.HasActions;

public abstract class HyperJob<F> implements WorkflowElement, HasActions, Cloneable {
	
	protected double[] dimensions = new double[4];
	
	protected HyperWorkflow<F> parent;
	
	@Override
	public HyperJob<F> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked")
		HyperJob<F> cl = (HyperJob<F>) super.clone();
		cl.dimensions = Arrays.copyOf(dimensions, 4);
		return cl;
	}
	
	public double getHeight() {
		return dimensions[3];
	}

	@Override
	public abstract List<Port> getInputPorts();

	public abstract String getName();
	
	public HyperWorkflow<F> getParent() {
		return parent;
	}

	@Override
	public abstract List<Port> getOutputPorts();
	
	public abstract Class<F> getFragmentType();

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

	public abstract List<HyperJob<F>> unfold() throws CloneNotSupportedException;

	public abstract <R> R selectRenderer(RendererAssortment<R> ra);
	
	/** { x, y, width, height } */
	public void setDimensions(double[] d) {
		assert(d.length == 4);
		System.arraycopy(d, 0, dimensions, 0, 4);
	}

}
