package org.vanda.studio.model.hyper;

import java.util.List;

import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.workflows.InputPort;
import org.vanda.studio.model.workflows.OutputPort;
import org.vanda.studio.model.workflows.RendererAssortment;
import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.util.Action;

public class AtomicHyperJob<F> extends HyperJob<F> {
	private final Tool<F> tool;

	public AtomicHyperJob(Tool<F> tool) {
		this.tool = tool;
	}
	
	@Override
	public AtomicHyperJob<F> clone() throws CloneNotSupportedException {
		return (AtomicHyperJob<F>) super.clone();
	}

	@Override
	public List<Port> getInputPorts() {
		return tool.getInputPorts();
	}

	@Override
	public List<Port> getOutputPorts() {
		return tool.getOutputPorts();
	}

	@Override
	public boolean isInputPort() {
		return tool instanceof InputPort;
	}

	@Override
	public boolean isOutputPort() {
		return tool instanceof OutputPort;
	}

	@Override
	public List<HyperJob<F>> unfold() {
		return null;
	}

	@Override
	public Class<F> getFragmentType() {
		return tool.getFragmentType();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return tool.selectRenderer(ra);
	}

	@Override
	public String getName() {
		return tool.getName();
	}

	@Override
	public void appendActions(List<Action> as) {
		tool.appendActions(as);
	}

	public static <F> HyperJob<F> create(Tool<F> item) {
		return new AtomicHyperJob<F>(item);
	}
}
