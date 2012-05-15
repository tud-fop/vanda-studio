package org.vanda.studio.model.elements;

import java.util.Collections;
import java.util.List;

import org.vanda.studio.util.Action;

public class OutputPort implements Element {
	
	int number;

	public OutputPort(int number) {
		this.number = number;
	}

	@Override
	public void appendActions(List<Action> as) {
		
	}

	@Override
	public Element clone() {
		return new OutputPort(number);
	}

	@Override
	public Class<?> getFragmentType() {
		return null;
	}

	@Override
	public String getId() {
		return "outport";
	}

	@Override
	public List<Port> getInputPorts() {
		return Ports.outputPortInputs;
	}

	@Override
	public String getName() {
		return "outport["+Integer.toString(number)+"]";
	}
	
	public int getNumber() {
		return number;
	}

	@Override
	public List<Port> getOutputPorts() {
		return Collections.emptyList();
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectOutputPortRenderer();
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
		return "";
	}

}
