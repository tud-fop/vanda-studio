package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.vanda.studio.model.RendererSelection;

/**
 * 
 * @author afischer
 */
public final class Or extends Element {

	public Or(String name) {
		this(null, name);
	}
	
	/**
	 * Copy constructor - makes a deep copy of the specified Or except for the
	 * parent attribute where only its reference is copied
	 * 
	 * @param toCopy
	 */
	public Or(Or toCopy) {
		super(toCopy);
	}
	
	// creates two inputs and one output by default
	public Or(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
		getInputPorts().add(
				new Port("in" + getInputPorts().size(), "type"));
		getInputPorts().add(
				new Port("in" + getInputPorts().size(), "type"));
		getOutputPorts().add(
				new Port("out" + getOutputPorts().size(), "type"));
	}
	
	/**
	 * Copy constructor - makes a deep copy of the specified Or except for the
	 * parent attribute where only its reference is copied In addition, the
	 * parent of the returned copy is set to another NestedHyperworkflow.
	 * 
	 * @param toCopy
	 */
	public Or(Or toCopy, NestedHyperworkflow newParent) {
		this(toCopy);
		setParent(newParent);
	}
	
	public Or(NestedHyperworkflow parent, String name, List<Port> inputPorts,
			List<Port> outputPorts) {
		super(parent, name, inputPorts, outputPorts);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return new Or(this);
	}

	@Override
	public void selectRenderer(RendererSelection rs) {
		rs.selectOrRenderer();
	}

	@Override
	public Collection<Hyperworkflow> unfold() {
		return Collections.emptyList();
	}
}
