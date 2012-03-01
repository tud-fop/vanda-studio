package org.vanda.studio.modules.workflows;

import java.util.Collection;
import java.util.List;

/**
 * Superclass component of IHyperworkflow composite pattern
 * @author afischer
 *
 */
public interface IHyperworkflow {

	public boolean equals(Object other);
	/** @return the id of the current Hyperworkflow */
	public int getId();
	/** @return a list of input ports */
	public List<Port> getInputPorts();	
	/** @return the name of the current Hyperworkflow */
	public String getName();
	/** @return a list of output ports */
	public List<Port> getOutputPorts();
	/** @return the NestedHyperworkflow that contains the current Hyperworkflow */
	public NestedHyperworkflow getParent();
	/** @return a (duplicate-free) collection of NestedHyperworkflows where all OR nodes have been removed */
	public Collection<IHyperworkflow> unfold();
}
