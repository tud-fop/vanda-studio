package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Leaf of Hyperworkflow composite pattern
 * 
 * @author afischer
 */
public class Job extends Hyperworkflow {

	public Job(String name) {
		this(null, name);
	}

	/**
	 * Copy constructor - makes a deep copy of the specified Tool except for the
	 * parent attribute where only its reference is copied
	 * 
	 * @param toCopy
	 */
	public Job(Job toCopy) {
		super(toCopy);
	}

	public Job(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
	}

	public Job(NestedHyperworkflow parent, String name, List<Port> inputPorts,
			List<Port> outputPorts) {
		super(parent, name, inputPorts, outputPorts);
	}

	public Object clone() throws CloneNotSupportedException {
		return new Job(this);
	}

	@Override
	public Collection<Hyperworkflow> unfold() {
		return Collections.singletonList((Hyperworkflow)new Job(this));
	}
}
