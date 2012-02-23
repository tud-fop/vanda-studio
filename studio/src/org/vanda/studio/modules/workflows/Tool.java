package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author afischer
 *
 */
public class Tool implements IElement{

	private NestedHyperworkflow parent;
	private String name;
	private int id;
	private List<Port> inputPorts;
	private List<Port> outputPorts;
	private Map<Port, Connection> portIncomingConnectionMap;
	
	public Tool(NestedHyperworkflow parent, String name, int id, List<Port> inputPorts, List<Port> outputPorts) {
		this.parent = parent;
		this.name = name;
		this.id = id;
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
		this.portIncomingConnectionMap = new HashMap<Port, Connection>();
	}
	
	public Tool(NestedHyperworkflow parent, String name, int id) {
		this(parent, name, id, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	/** 
	 * Copy constructor
	 * @param toCopy
	 */
	public Tool(Tool toCopy) {
		this(toCopy.parent, toCopy.name, toCopy.id, new ArrayList<Port>(toCopy.inputPorts), new ArrayList<Port>(toCopy.outputPorts));
		portIncomingConnectionMap = new HashMap<Port, Connection>(toCopy.getPortIncomingConnectionMap());
	}
	
	public NestedHyperworkflow getParent() { return parent; }
	public List<Port> getOutputPorts() {	return outputPorts; }
	public Map<Port, Connection> getPortIncomingConnectionMap() { return portIncomingConnectionMap; }
	public int getId() {	return id; }
	public List<Port> getInputPorts() { return inputPorts;	}
	public String getName() { return name; }
	
	@Override
	public boolean equals(Object other) {
		//FIXME think of something more reasonable to find equal Tools
		//Tools are equal if they have the same id
		boolean result = (other != null && other instanceof Tool);
		if (result) {
			Tool oh = (Tool)other;
			result = (this.getId() == oh.getId());
		}
		return result;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public Collection<IHyperworkflow> unfold() {
		List<IHyperworkflow> singletonToolList = new ArrayList<IHyperworkflow>();
		singletonToolList.add(this);
		return singletonToolList;
	}
}
