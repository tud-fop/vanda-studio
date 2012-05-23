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
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

public abstract class Job implements HasActions, Cloneable {
	
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
	
	/**
	 * may return null if name is immutable
	 * @return
	 */
	public abstract Observable<Job> getNameChangeObservable();

	public abstract List<Port> getOutputPorts();
	
	/**
	 * may return null if ports are immutable
	 * @return
	 */
	public abstract Observable<Pair<Job, Integer>> getAddInputPortObservable();

	/**
	 * may return null if ports are immutable
	 * @return
	 */
	public abstract Observable<Pair<Job, Integer>> getAddOutputPortObservable();

	/**
	 * may return null if ports are immutable
	 * @return
	 */
	public abstract Observable<Pair<Job, Integer>> getRemoveInputPortObservable();

	/**
	 * may return null if ports are immutable
	 * @return
	 */
	public abstract Observable<Pair<Job, Integer>> getRemoveOutputPortObservable();

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
