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
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.TokenSource.Token;

public class CompositeJob extends Job {

	private final Linker linker;

	private MutableWorkflow workflow; // not final because of clone()

	public CompositeJob(Linker linker, MutableWorkflow workflow) {
		address = null;
		this.linker = linker;
		this.workflow = workflow;
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
		return null; // TODO change this once linkers become mutable
	}

	@Override
	public Observable<Job> getPortsChangeObservable() {
		return null; // TODO change this once linkers become mutable
	}

	@Override
	public void rebind() {
		// do nothing
	}

	@Override
	public RepositoryItem getItem() {
		return linker;
	}

}
