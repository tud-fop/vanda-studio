package org.vanda.studio.model.elements;

import java.util.Collections;
import java.util.List;

import org.vanda.studio.util.Action;

public class Literal implements Element {
	
	String type;
	String value;

	public Literal(String type, String value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public void appendActions(List<Action> as) {

	}
	
	@Override
	public Element clone() {
		return new Literal(type, value);
	}

	@Override
	public Class<?> getFragmentType() {
		return null;
	}

	@Override
	public String getId() {
		return "literal";
	}

	@Override
	public List<Port> getInputPorts() {
		return Collections.emptyList();
	}

	@Override
	public String getName() {
		return "literal["+value+"]";
	}

	@Override
	public List<Port> getOutputPorts() {
		return Ports.literalOutputs;
	}

	@Override
	public <R> R selectRenderer(RendererAssortment<R> ra) {
		return ra.selectLiteralRenderer();
	}

	@Override
	public String getCategory() {
		return "basics";
	}

}
