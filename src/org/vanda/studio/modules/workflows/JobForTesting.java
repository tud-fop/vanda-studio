package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.vanda.studio.model.Port;

/**
 * Leaf of Hyperworkflow composite pattern
 * 
 * @author afischer
 */
public class JobForTesting extends Hyperworkflow {

	public JobForTesting(String name) {
		this(null, name);
	}

	/**
	 * Copy constructor - makes a deep copy of the specified Tool except for the
	 * parent attribute where only its reference is copied
	 * 
	 * @param toCopy
	 */
	public JobForTesting(JobForTesting toCopy) {
		super(toCopy);
	}

	public JobForTesting(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
	}

	public JobForTesting(NestedHyperworkflow parent, String name, List<Port> inputPorts,
			List<Port> outputPorts) {
		super(parent, name, inputPorts, outputPorts);
	}

	public Object clone() throws CloneNotSupportedException {
		return new JobForTesting(this);
	}

	@Override
	public Collection<Hyperworkflow> unfold() {
		return Collections.singletonList((Hyperworkflow)new JobForTesting(this));
	}
}
