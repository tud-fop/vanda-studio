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
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

public class AtomicJob extends Job {
	private final Element element;
	private final MultiplexObserver<Job> nameChangeObservable;
	private final MultiplexObserver<Pair<Job, Integer>> addPortObservable;
	private final MultiplexObserver<Pair<Job, Integer>> removePortObservable;

	public AtomicJob(Element element) {
		address = null;
		this.element = element;
		if (element.getNameChangeObservable() != null)
			nameChangeObservable = new MultiplexObserver<Job>();
		else
			nameChangeObservable = null;
		if (element.getAddPortObservable() != null)
			addPortObservable = new MultiplexObserver<Pair<Job, Integer>>();
		else
			addPortObservable = null;
		if (element.getRemovePortObservable() != null)
			removePortObservable = new MultiplexObserver<Pair<Job, Integer>>();
		else
			removePortObservable = null;
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
	public Observable<Pair<Job, Integer>> getAddPortObservable() {
		return addPortObservable;
	}

	@Override
	public Observable<Pair<Job, Integer>> getRemovePortObservable() {
		return removePortObservable;
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
		if (addPortObservable != null)
			element.getAddPortObservable().addObserver(
					new Observer<Pair<Element, Integer>>() {
						@Override
						public void notify(Pair<Element, Integer> event) {
							addPortObservable.notify(new Pair<Job, Integer>(
									AtomicJob.this, event.snd));
						}
					});
		if (removePortObservable != null)
			element.getRemovePortObservable().addObserver(
					new Observer<Pair<Element, Integer>>() {
						@Override
						public void notify(Pair<Element, Integer> event) {
							removePortObservable.notify(new Pair<Job, Integer>(
									AtomicJob.this, event.snd));
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
