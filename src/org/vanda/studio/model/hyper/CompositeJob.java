package org.vanda.studio.model.hyper;

import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RendererAssortment;
import org.vanda.studio.model.immutable.CompositeImmutableJob;
import org.vanda.studio.model.immutable.ImmutableJob;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.TokenSource.Token;

public class CompositeJob<IF, F> extends Job<F> {

	private final Linker<IF, F> linker;

	private MutableWorkflow<IF> workflow; // not final because of clone()

	public CompositeJob(Linker<IF, F> linker, MutableWorkflow<IF> workflow) {
		address = null;
		this.linker = linker;
		this.workflow = workflow;
	}

	@Override
	public CompositeJob<IF, F> clone() throws CloneNotSupportedException {
		return new CompositeJob<IF, F>(linker, workflow.clone());
	}

	@Override
	public List<Port> getInputPorts() {
		return workflow.getInputPorts();
	}

	@Override
	public List<Port> getOutputPorts() {
		return workflow.getOutputPorts();
	}

	@Override
	public ImmutableJob<F> freeze() throws Exception {
		return new CompositeImmutableJob<IF, F>(linker, workflow.freeze());
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
	public Class<F> getFragmentType() {
		return linker.getFragmentType();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectBoxRenderer();
	}

	@Override
	public String getName() {
		return linker.getName();
	}

	public MutableWorkflow<IF> getWorkflow() {
		return workflow;
	}

	@Override
	public void appendActions(List<Action> as) {
		linker.appendActions(as);
	}

	@Override
	public HyperWorkflow<?> dereference(ListIterator<Token> address) {
		return workflow.dereference(address);
	}

	@Override
	public String getContact() {
		return linker.getContact();
	}

	@Override
	public String getCategory() {
		return linker.getCategory();
	}

	@Override
	public String getDescription() {
		return linker.getDescription();
	}
	
	public Linker<IF, F> getLinker() {
		return linker;
	}

}
