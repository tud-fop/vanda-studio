package org.vanda.studio.model.elements;

import java.util.Collections;
import java.util.List;

import org.vanda.studio.util.Action;

public final class InputPort implements Element {
	
	private int number;

	public InputPort(int number) {
		this.number = number;
	}

	@Override
	public void appendActions(List<Action> as) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Element clone() {
		return new InputPort(number);
	}

	@Override
	public Class<?> getFragmentType() {
		return null;
	}

	@Override
	public String getId() {
		return "inport";
	}

	@Override
	public List<Port> getInputPorts() {
		return Collections.emptyList();
	}

	@Override
	public String getName() {
		return "inport["+Integer.toString(number)+"]";
	}

	@Override
	public List<Port> getOutputPorts() {
		return Ports.inputPortOutputs;
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectGrammarRenderer(); // FIXME!!!
	}

	@Override
	public String getCategory() {
		return "basics";
	}

}
