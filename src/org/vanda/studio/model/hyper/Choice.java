package org.vanda.studio.model.hyper;

import java.util.List;

import org.vanda.studio.model.generation.Artifacts;
import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.workflows.RendererAssortment;
import org.vanda.studio.util.Action;

public final class Choice<V> extends HyperJob<V> {

	private int inputs;

	@Override
	public List<Port> getInputPorts() {
		return Artifacts.getChoiceInputPorts(inputs);
	}

	@Override
	public List<Port> getOutputPorts() {
		return Artifacts.identityOutputs;
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
	public List<HyperJob<V>> unfold() {
		//return Collections.singletonList((HyperJob<V>) new IdentityJob<V>(this));
		return null; // XXX
	}

	public void setInputPorts(int inputs) {
		// TODO notify
		this.inputs = inputs;
	}

	@Override
	public Class<V> getFragmentType() {
		return null; // FIXME this may not be working
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectOrRenderer();
	}

	@Override
	public String getName() {
		return "v";
	}

	@Override
	public void appendActions(List<Action> as) {
		// do nothing
	}

}
