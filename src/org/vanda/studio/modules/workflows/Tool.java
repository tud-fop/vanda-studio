package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author afischer
 */
public class Tool extends Element {

	public Tool(String name) {
		this(null, name);
	}

	/**
	 * Copy constructor - makes a deep copy of the specified Tool except for the
	 * parent attribute where only its reference is copied
	 * 
	 * @param toCopy
	 */
	public Tool(Tool toCopy) {
		super(toCopy);
	}

	public Tool(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
	}

	public Tool(NestedHyperworkflow parent, String name, List<Port> inputPorts,
			List<Port> outputPorts) {
		super(parent, name, inputPorts, outputPorts);
	}

	public Object clone() throws CloneNotSupportedException {
		return new Tool(this);
	}

	@Override
	public Collection<Hyperworkflow> unfold() {
		List<Hyperworkflow> singletonToolList = new ArrayList<Hyperworkflow>();
		singletonToolList.add(new Tool(this));
		return singletonToolList;
	}
}
