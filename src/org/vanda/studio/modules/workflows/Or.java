package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.vanda.studio.model.RendererSelection;

/**
 * Leaf of Hyperworkflow composite pattern
 * 
 * @author afischer
 */
public final class Or extends Hyperworkflow {

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
	public boolean equals(Object other) {
		// make sure other object is an OR node
		boolean result = (other instanceof Or);
		if (result) {
			Or otherOr = (Or) other;
			
			// compare attributes (except parent)
			//XXX maybe test for equal id is sufficient?
			result = (getId() == otherOr.getId()
					&& getName().equals(otherOr.getName())
					&& getInputPorts().equals(otherOr.getInputPorts()) 
					&& getOutputPorts().equals(otherOr.getOutputPorts()));
		}
		return result;
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
