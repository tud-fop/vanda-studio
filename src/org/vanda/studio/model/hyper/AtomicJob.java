package org.vanda.studio.model.hyper;

import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Element;
import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.OutputPort;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.immutable.AtomicImmutableJob;
import org.vanda.studio.model.immutable.ImmutableJob;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource.Token;

public class AtomicJob<F> extends Job<F> {
	private final Element element;
	private final MultiplexObserver<Job<F>> nameChangeObservable;
	private final MultiplexObserver<Job<F>> portsChangeObservable;

	public AtomicJob(Element element) {
		address = null;
		this.element = element;
		Observable<Element> obs = element.getNameChangeObservable();
		if (obs != null) {
			nameChangeObservable = new MultiplexObserver<Job<F>>();
			obs.addObserver(new Observer<Element>() {
				@Override
				public void notify(Element event) {
					nameChangeObservable.notify(AtomicJob.this);
				}});
		} else
			nameChangeObservable = null;
		obs = element.getPortsChangeObservable();
		if (obs != null) {
			portsChangeObservable = new MultiplexObserver<Job<F>>();
			obs.addObserver(new Observer<Element>() {
				@Override
				public void notify(Element event) {
					portsChangeObservable.notify(AtomicJob.this);
				}});
		} else
			portsChangeObservable = null;
	}
	
	@Override
	public AtomicJob<F> clone() throws CloneNotSupportedException {
		return new AtomicJob<F>(element.clone());
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
	public ImmutableJob<F> freeze() {
		return new AtomicImmutableJob<F>(element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<F> getFragmentType() {
		// XXX weakness
		return (Class<F>) element.getFragmentType();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return element.selectRenderer(ra);
	}

	@Override
	public String getName() {
		return element.getName();
	}

	@Override
	public void appendActions(List<Action> as) {
		element.appendActions(as);
	}

	@Override
	public HyperWorkflow<?> dereference(ListIterator<Token> address) {
		return null;
	}

	@Override
	public String getContact() {
		return element.getContact();
	}

	@Override
	public String getCategory() {
		return element.getCategory();
	}

	@Override
	public String getDescription() {
		return element.getDescription();
	}

	@Override
	public Observable<Job<F>> getNameChangeObservable() {
		return nameChangeObservable;
	}

	@Override
	public Observable<Job<F>> getPortsChangeObservable() {
		return portsChangeObservable;
	}
}
