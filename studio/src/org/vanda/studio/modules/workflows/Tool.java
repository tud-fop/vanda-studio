package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author afischer
 */
public class Tool extends Hyperworkflow {

	private List<Port> inputPorts;
	private List<Port> outputPorts;
	private Map<Port, Connection> inputConnectionMap;

	public Tool(NestedHyperworkflow parent, String name, int id) {
		super(parent, name, id);
		inputPorts = new ArrayList<Port>();
		outputPorts = new ArrayList<Port>();
		inputConnectionMap = new HashMap<Port, Connection>();
	}
	
	/**
	 * @return a list of input ports
	 */
	public List<Port> getInputPorts() {
		return inputPorts;
	}

	/**
	 * @return a list of output ports
	 */
	public List<Port> getOutputPorts() {
		return outputPorts;
	}
	
	/** 
	 * Returns the map that contains for every blocked input port its incoming connection
	 * @return 
	 */
	public Map<Port, Connection> getInputBlockageMap() {
		return inputConnectionMap;
	}

	@Override
	public void unfold() {
		//TODO
	}
}
