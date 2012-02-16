package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author afischer
 */
public abstract class Hyperworkflow {
	private NestedHyperworkflow parent;
	private String name;
	private int id;
	private List<Port> inputPorts;
	private List<Port> outputPorts;
	private Map<Port, Connection> portIncomingConnectionMap;
	
	public Hyperworkflow(NestedHyperworkflow parent, String name, int id, List<Port> inputPorts, List<Port> outputPorts) {
		this.parent = parent;
		this.name = name;
		this.id = id;
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
		this.portIncomingConnectionMap = new HashMap<Port, Connection>();
	}
	
	public Hyperworkflow(NestedHyperworkflow parent, String name, int id) {
		this(parent, name, id, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	/**
	 * @return the NestedHyperworkflow that contains the current Hyperworkflow
	 */
	public NestedHyperworkflow getParent() {
		return parent;
	}

	/**
	 * @return the name of the current Hyperworkflow
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the id of the current Hyperworkflow
	 */
	public int getId() {
		return id;
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
	public Map<Port, Connection> getPortIncomingConnectionMap() {
		return portIncomingConnectionMap;
	}
	
	@Override
	public boolean equals(Object other) {
		//FIXME think of something more reasonable to find equal Hyperworkflows
		//Hyperworkflows are equal if they have the same id
		boolean result = (other != null && other instanceof Hyperworkflow);
		if (result) {
			Hyperworkflow oh = (Hyperworkflow)other;
			result = (id == oh.id);
		}
		return result;
	}
	
	public abstract void unfold();
}
