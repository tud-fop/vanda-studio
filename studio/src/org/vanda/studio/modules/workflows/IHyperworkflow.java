package org.vanda.studio.modules.workflows;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IHyperworkflow {

	/** @return an instance of IHyperworkflow */
	public IHyperworkflow createHyperworkflowInstance(NestedHyperworkflow parent, String name, int id, List<Port> inputPorts, List<Port> outputPorts);
	/** @return an instance of IHyperworkflow */
	public IHyperworkflow createHyperworkflowInstance(NestedHyperworkflow parent, String name, int id);
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
	/** @return the map that contains for every blocked input port its incoming connection */
	public Map<Port, Connection> getPortIncomingConnectionMap();
	/** @return a collection of NestedHyperworkflows where all OR nodes have been removed */
	public Collection<NestedHyperworkflow> unfold();
}
