package org.vanda.studio.model.hyper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.workflows.Linker;
import org.vanda.studio.model.workflows.RendererAssortment;
import org.vanda.studio.util.Action;

public class CompositeHyperJob<IF, F> extends
		HyperJob<F> {

	private final Linker<IF, F> linker;

	private HyperWorkflow<IF> workflow; // not final because of clone()

	public CompositeHyperJob(Linker<IF, F> linker,
			HyperWorkflow<IF> workflow) {
		this.linker = linker;
		this.workflow = workflow;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CompositeHyperJob<IF, F> clone()
			throws CloneNotSupportedException {
		return (CompositeHyperJob<IF, F>) super
				.clone();
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
	public List<HyperJob<F>> unfold() throws CloneNotSupportedException {
		List<HyperWorkflow<IF>> workflows = workflow.unfold();
		List<HyperJob<F>> jobs = new LinkedList<HyperJob<F>>();
		for (HyperWorkflow<IF> w : workflows)
			jobs.add(new CompositeHyperJob<IF, F>(linker, w));
		return jobs;
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
		return linker.getViewType();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectAlgorithmRenderer();
	}

	@Override
	public String getName() {
		return "";
	}

	public HyperWorkflow<IF> getWorkflow() {
		return workflow;
	}

	@Override
	public void appendActions(List<Action> as) {
		linker.appendActions(as);
	}

}
