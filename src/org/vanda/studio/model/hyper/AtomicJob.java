package org.vanda.studio.model.hyper;

import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Element;
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

public class AtomicJob extends Job {
	private final Element element;
	private final MultiplexObserver<Job> nameChangeObservable;
	private final MultiplexObserver<Job> portsChangeObservable;

	public AtomicJob(Element element) {
		address = null;
		this.element = element;
		if (element.getNameChangeObservable() != null)
			nameChangeObservable = new MultiplexObserver<Job>();
		else
			nameChangeObservable = null;
		if (element.getPortsChangeObservable() != null)
			portsChangeObservable = new MultiplexObserver<Job>();
		else
			portsChangeObservable = null;
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
	public Observable<Job> getNameChangeObservable() {
		return nameChangeObservable;
	}

	@Override
	public Observable<Job> getPortsChangeObservable() {
		return portsChangeObservable;
	}

	@Override
	public void rebind() {
		if (nameChangeObservable != null)
			element.getNameChangeObservable().addObserver(
					new Observer<Element>() {
						@Override
						public void notify(Element event) {
							nameChangeObservable.notify(AtomicJob.this);
						}
					});
		if (portsChangeObservable != null)
			element.getPortsChangeObservable().addObserver(
					new Observer<Element>() {
						@Override
						public void notify(Element event) {
							portsChangeObservable.notify(AtomicJob.this);
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
}
