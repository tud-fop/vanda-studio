package org.vanda.studio.model.hyper;

import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.immutable.CompositeImmutableJob;
import org.vanda.studio.model.immutable.ImmutableJob;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Pair;
import org.vanda.studio.util.TokenSource.Token;

public class CompositeJob extends Job {

	private final Linker linker;

	private MutableWorkflow workflow; // not final because of clone()

	private final MultiplexObserver<Pair<Job, Integer>> addPortObservable;

	private final MultiplexObserver<Pair<Job, Integer>> removePortObservable;

	public CompositeJob(Linker linker, MutableWorkflow workflow) {
		address = null;
		this.linker = linker;
		this.workflow = workflow;
		addPortObservable = new MultiplexObserver<Pair<Job, Integer>>();
		removePortObservable = new MultiplexObserver<Pair<Job, Integer>>();
	}

	@Override
	public CompositeJob clone() throws CloneNotSupportedException {
		return new CompositeJob(linker, workflow.clone());
	}

	@Override
	public List<Port> getInputPorts() {
		return linker.convertInputPorts(workflow.getInputPorts());
	}

	@Override
	public List<Port> getOutputPorts() {
		return linker.convertOutputPorts(workflow.getOutputPorts());
	}

	@Override
	public ImmutableJob freeze() throws Exception {
		return new CompositeImmutableJob(address, linker, workflow.freeze());
	}

	@Override
	public boolean isInputPort() {
		return false;
	}

	@Override
	public boolean isOutputPort() {
		return false;
	}

	@Override
	public Type getFragmentType() {
		return linker.getFragmentType();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectBoxRenderer();
	}

	public MutableWorkflow getWorkflow() {
		return workflow;
	}

	@Override
	public void appendActions(List<Action> as) {
		linker.appendActions(as);
	}

	@Override
	public MutableWorkflow dereference(ListIterator<Token> address) {
		return workflow.dereference(address);
	}

	public Linker getLinker() {
		return linker;
	}

	@Override
	public Observable<Job> getNameChangeObservable() {
		return null; // XXX change this once linkers become mutable
	}

	@Override
	public void rebind() {
		workflow.getAddPortObservable().addObserver(
				new Observer<Pair<MutableWorkflow, Integer>>() {
					@Override
					public void notify(Pair<MutableWorkflow, Integer> event) {
						addPortObservable.notify(new Pair<Job, Integer>(
								CompositeJob.this, event.snd));
					}
				});
		workflow.getRemovePortObservable().addObserver(
				new Observer<Pair<MutableWorkflow, Integer>>() {
					@Override
					public void notify(Pair<MutableWorkflow, Integer> event) {
						removePortObservable.notify(new Pair<Job, Integer>(
								CompositeJob.this, event.snd));
					}
				});
	}

	@Override
	public RepositoryItem getItem() {
		return linker;
	}

	@Override
	public void visit(JobVisitor v) {
		v.visitCompositeJob(this);
	}

	@Override
	public Observable<Pair<Job, Integer>> getAddPortObservable() {
		return addPortObservable;
	}

	@Override
	public Observable<Pair<Job, Integer>> getRemovePortObservable() {
		return removePortObservable;
	}

}
