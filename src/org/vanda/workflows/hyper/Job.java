package org.vanda.workflows.hyper;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.types.Type;
import org.vanda.util.HasActions;
import org.vanda.util.Observable;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.elements.RepositoryItem;
import org.vanda.workflows.immutable.ImmutableJob;

public abstract class Job implements HasActions, Cloneable {
	
	public static interface JobListener {
		// removed: see older versions
		// void inputPortAdded(Job j, int index);
		// void inputPortRemoved(Job j, int index);
		// void outputPortAdded(Job j, int index);
		// void outputPortRemoved(Job j, int index);
		void propertyChanged(Job j);		
	}
	
	public static interface JobEvent {
		void doNotify(JobListener jl);
	}
	
	public final ArrayList<Token> inputs = new ArrayList<Token>();
	
	public final ArrayList<Token> outputs = new ArrayList<Token>();
	
	protected Token address;

	protected final double[] dimensions = new double[4];
	
	@Override
	public abstract Job clone() throws CloneNotSupportedException;

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
