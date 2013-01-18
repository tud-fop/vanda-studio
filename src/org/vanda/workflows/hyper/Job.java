package org.vanda.workflows.hyper;

import java.util.List;

import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.Element;
import org.vanda.workflows.elements.Element.ElementListener;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.elements.Element.ElementEvent;
import org.vanda.workflows.immutable.ImmutableJob;

public final class Job implements Cloneable, ElementListener {
	public static interface JobEvent {
		void doNotify(JobListener jl);
	}
	
	public static interface JobListener {
		// removed: see older versions
		// void inputPortAdded(Job j, int index);
		// void inputPortRemoved(Job j, int index);
		// void outputPortAdded(Job j, int index);
		// void outputPortRemoved(Job j, int index);
		void propertyChanged(Job j);		
	}
	
	protected Token address;
	private final Element element;
	private final MultiplexObserver<JobEvent> observable;
	protected Token[] inputs = null;
	public Token[] outputs = null;
	protected final double[] dimensions = new double[4];
	
	public Job(Element element) {
		address = null;
		this.element = element;
		if (element.getObservable() != null)
			observable = new MultiplexObserver<JobEvent>();
		else
			observable = null;
		rebind();
	}
	
	@Override
	public Job clone() throws CloneNotSupportedException {
		return new Job(element.clone());
	}

	public ImmutableJob freeze() {
		return new ImmutableJob(address, getElement());
	}

	public Token getAddress() {
		return address;
	}

	public Element getElement() {
		return element;
	}

	public Type getFragmentType() {
		return getElement().getFragmentType();
	}

	public double getHeight() {
		return dimensions[3];
	}

	public List<Port> getInputPorts() {
		return getElement().getInputPorts();
	}
	
	public String getName() {
		return element.getName();
	}

	public Observable<JobEvent> getObservable() {
		return observable;
	}

	public List<Port> getOutputPorts() {
		return getElement().getOutputPorts();
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
	
	public void insert(Token address) {
		assert (this.address == null);
		this.address = address;
		inputs = new Token[getInputPorts().size()];
		outputs = new Token[getOutputPorts().size()];
	}

	public void propertyChanged(Element e) {
		observable.notify(new Jobs.PropertyChangedEvent(this));
	}

	/**
	 *  call this after deserialization
	 */
	public void rebind() {
		if (observable != null)
			getElement().getObservable().addObserver(new Observer<ElementEvent>() {
				@Override
				public void notify(ElementEvent event) {
					event.doNotify(Job.this);
				}
			});
	}
	
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return getElement().selectRenderer(ra);
	}

	/** { x, y, width, height } */
	public void setDimensions(double[] d) {
		assert(d.length == 4);
		System.arraycopy(d, 0, dimensions, 0, 4);
	}
	
	public void visit(ElementVisitor v) {
		getElement().visit(v);
	}

}
