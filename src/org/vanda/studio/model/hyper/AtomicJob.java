package org.vanda.studio.model.hyper;

import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Element;
import org.vanda.studio.model.elements.Element.ElementEvent;
import org.vanda.studio.model.elements.Element.ElementListener;
import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.OutputPort;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.immutable.AtomicImmutableJob;
import org.vanda.studio.model.immutable.ImmutableJob;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource.Token;

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
	public boolean isInputPort() {
		return element instanceof InputPort;
	}

	@Override
	public boolean isOutputPort() {
		return element instanceof OutputPort;
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
	public void inputPortAdded(Element e, int index) {
		observable.notify(new Jobs.InputPortAddedEvent(this, index));
	}

	@Override
	public void inputPortRemoved(Element e, int index) {
		observable.notify(new Jobs.InputPortRemovedEvent(this, index));
	}

	@Override
	public void propertyChanged(Element e) {
		observable.notify(new Jobs.PropertyChangedEvent(this));
	}
}
