package org.vanda.workflows.hyper;

import java.util.List;
import java.util.ListIterator;

import org.vanda.types.Type;
import org.vanda.util.Action;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.Element;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.RendererAssortment;
import org.vanda.workflows.elements.RepositoryItem;
import org.vanda.workflows.elements.Element.ElementEvent;
import org.vanda.workflows.elements.Element.ElementListener;
import org.vanda.workflows.immutable.AtomicImmutableJob;
import org.vanda.workflows.immutable.ImmutableJob;

public class AtomicJob extends Job implements ElementListener {
	private final Element element;
	private final MultiplexObserver<JobEvent> observable;

	public AtomicJob(Element element) {
		address = null;
		this.element = element;
		if (element.getObservable() != null)
			observable = new MultiplexObserver<JobEvent>();
		else
			observable = null;
		rebind();
	}

	@Override
	public AtomicJob clone() throws CloneNotSupportedException {
		return new AtomicJob(element.clone());
		// TODO clone dimensions
	}

	public Element getElement() {
		return element;
	}

	@Override
	public List<Port> getInputPorts() {
		return element.getInputPorts();
	}

	@Override
	public List<Port> getOutputPorts() {
		return element.getOutputPorts();
	}

	@Override
	public ImmutableJob freeze() {
		return new AtomicImmutableJob(address, element);
	}

	@Override
	public Type getFragmentType() {
		return element.getFragmentType();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return element.selectRenderer(ra);
	}

	@Override
	public void appendActions(List<Action> as) {
		element.appendActions(as);
	}

	@Override
	public MutableWorkflow dereference(ListIterator<Token> address) {
		return null;
	}

	@Override
	public void rebind() {
		if (observable != null)
			element.getObservable().addObserver(new Observer<ElementEvent>() {
				@Override
				public void notify(ElementEvent event) {
					event.doNotify(AtomicJob.this);
				}
			});
	}

	@Override
	public RepositoryItem getItem() {
		return element;
	}

	@Override
	public void visit(JobVisitor v) {
		v.visitAtomicJob(this);
	}

	@Override
	public Observable<JobEvent> getObservable() {
		return observable;
	}

	@Override
	public void propertyChanged(Element e) {
		observable.notify(new Jobs.PropertyChangedEvent(this));
	}
}
