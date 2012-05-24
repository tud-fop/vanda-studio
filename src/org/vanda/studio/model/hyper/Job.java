package org.vanda.studio.model.hyper;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.immutable.ImmutableJob;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.util.HasActions;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.TokenSource.Token;

public abstract class Job implements HasActions, Cloneable {
	
	public static interface JobListener {
		void inputPortAdded(Job j, int index);
		void inputPortRemoved(Job j, int index);
		void outputPortAdded(Job j, int index);
		void outputPortRemoved(Job j, int index);
		void propertyChanged(Job j);		
	}
	
	public static interface JobEvent {
		void doNotify(JobListener jl);
	}
	
	
	protected Token address;

	protected double[] dimensions = new double[4];

	@Override
	public Job clone() throws CloneNotSupportedException {
		Job cl = (Job) super.clone();
		cl.dimensions = Arrays.copyOf(dimensions, 4);
		return cl;
	}

	public abstract MutableWorkflow dereference(ListIterator<Token> address);

	public double getHeight() {
		return dimensions[3];
	}

	public abstract List<Port> getInputPorts();
	
	public abstract Observable<JobEvent> getObservable();

	public abstract List<Port> getOutputPorts();
	
	public abstract Type getFragmentType();

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

	public abstract ImmutableJob freeze() throws Exception;

	public abstract <R> R selectRenderer(RendererAssortment<R> ra);

	/** { x, y, width, height } */
	public void setDimensions(double[] d) {
		assert(d.length == 4);
		System.arraycopy(d, 0, dimensions, 0, 4);
	}

	public Token getAddress() {
		return address;
	}
	
	public abstract RepositoryItem getItem();
	
	/**
	 *  call this after deserialization
	 */
	public abstract void rebind();
	
	public abstract void visit(JobVisitor v);

}
