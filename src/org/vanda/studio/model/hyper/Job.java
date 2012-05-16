package org.vanda.studio.model.hyper;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.immutable.ImmutableJob;
import org.vanda.studio.util.HasActions;
import org.vanda.studio.util.TokenSource.Token;

public abstract class Job<F> implements HasActions, Cloneable {
	
	protected double[] dimensions = new double[4];
	
	@Override
	public Job<F> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked")
		Job<F> cl = (Job<F>) super.clone();
		cl.dimensions = Arrays.copyOf(dimensions, 4);
		return cl;
	}
	
	public abstract HyperWorkflow<?> dereference(ListIterator<Token> address);
	
	public double getHeight() {
		return dimensions[3];
	}

	public abstract List<Port> getInputPorts();

	public abstract String getName();
	
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

	public abstract ImmutableJob<F> freeze() throws Exception;

	public abstract <R> R selectRenderer(RendererAssortment<R> ra);
	
	/** { x, y, width, height } */
	public void setDimensions(double[] d) {
		assert(d.length == 4);
		System.arraycopy(d, 0, dimensions, 0, 4);
	}

	public abstract String getContact();

	public abstract String getCategory();
	
	public abstract String getDescription();

}
