package org.vanda.studio.model.elements;

import java.util.List;

import org.vanda.studio.util.Action;

/**
 * Choice node. A choice node with one input port acts as identity.
 * 
 * @author buechse
 * 
 */
public final class Choice implements Element {

	private int inputs;

	public Choice() {
		this(2);
	}

	public Choice(int inputs) {
		this.inputs = inputs;
	}

	@Override
	public Element clone() {
		return new Choice(inputs);
	}

	@Override
	public List<Port> getInputPorts() {
		return Ports.getChoiceInputPorts(inputs);
	}

	@Override
	public List<Port> getOutputPorts() {
		return Ports.identityOutputs;
	}

	public void setInputPorts(int inputs) {
		// TODO notify
		this.inputs = inputs;
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectOrRenderer();
	}

	@Override
	public String getName() {
		return "CHOOSE";
	}

	@Override
	public void appendActions(List<Action> as) {
		// do nothing
	}

	@Override
	public Class<?> getFragmentType() {
		return null;
	}

	@Override
	public String getId() {
		return "OR";
	}

	@Override
	public String getCategory() {
		return "basics";
	}

	@Override
	public String getContact() {
		return "Vanda Studio Team";
	}

	@Override
	public String getDescription() {
		return "A CHOOSE node in a hyperworkflow determines several "
				+ "possibilities of generating workflows. Each possibility "
				+ "corresponds to choosing one of the incoming connections.";
	}

}
