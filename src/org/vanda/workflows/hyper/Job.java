package org.vanda.workflows.hyper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.ElementReturnVisitor;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ElementAdapters.ElementAdapterEvent;
import org.vanda.workflows.hyper.ElementAdapters.ElementAdapterListener;
import org.vanda.workflows.hyper.Jobs.*;

public class Job implements ElementAdapterListener<ElementAdapter> {
	private final ElementAdapter element;
	private final String id;
	public Map<Port, Location> bindings;
	protected final double[] dimensions = new double[4];
	protected final MultiplexObserver<JobEvent<Job>> observable;

	public Job(ElementAdapter element, String id) {
		this.id = id;
		this.element = element;
		if (element.getObservable() != null)
			observable = new MultiplexObserver<JobEvent<Job>>();
		else
			observable = null;
		rebind();
	}
	
	public Job(ElementAdapter element) {
		this(element, null);
	}
	
	public Job(ElementAdapter element, boolean createObservable) {
		this.id = null;
		this.element = element;
		this.observable = new MultiplexObserver<JobEvent<Job>>();
		rebind();
	}

	public ElementAdapter getElement() {
		return element;
	}

	public Type getFragmentType() {
		return element.getFragmentType();
	}

	public double getHeight() {
		return dimensions[3];
	}

	public String getId() {
		return id;
	}
	
	public List<Port> getInputPorts() {
		return element.getInputPorts();
	}

	public String getName() {
		return element.getName();
	}

	public Observable<JobEvent<Job>> getObservable() {
		return observable;
	}

	public List<Port> getOutputPorts() {
		return element.getOutputPorts();
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

	public void insert() {
		bindings = new HashMap<Port, Location>();
	}

	public boolean isConnected() {
		return bindings.keySet().containsAll(getInputPorts());
	}

	public boolean isInserted() {
		return bindings != null;
	}

	public void uninsert() {
		bindings = null;
	}

	/**
	 * call this after deserialization
	 */
	public void rebind() {
		Observable<ElementAdapterEvent<ElementAdapter>> o = element
				.getObservable();
		if (o != null)
			o.addObserver(new Observer<ElementAdapterEvent<ElementAdapter>>() {
				@Override
				public void notify(ElementAdapterEvent<ElementAdapter> event) {
					event.doNotify(Job.this);
				}
			});
		element.rebind();
	}

	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return element.selectRenderer(ra);
	}

	/** { x, y, width, height } */
	public void setDimensions(double[] d) {
		assert (d.length == 4);
		System.arraycopy(d, 0, dimensions, 0, 4);
	}

	public void visit(ElementVisitor v) {
		element.visit(v);
	}

	public void visit(final JobVisitor v) {
		element.visit(new ElementVisitor() {

			@Override
			public void visitLiteral(Literal l) {
				v.visitLiteral(Job.this, l);
			}

			@Override
			public void visitTool(Tool t) {
				v.visitTool(Job.this, t);
			}
		});
	}

	public <R> R visitReturn(ElementReturnVisitor<R> v) {
		return element.visitReturn(v);
	}

	public void addFragmentTypeEquation(TypeChecker tc) {
		tc.addFragmentTypeEquation(element.getFragmentType());
	}

	public void typeCheck() throws TypeCheckingException {
		// do nothing
	}

	@Override
	public void propertyChanged(ElementAdapter e) {
		// if (element.getObservable() != null)
		observable.notify(new PropertyChangedEvent<Job>(this));
	}

}
