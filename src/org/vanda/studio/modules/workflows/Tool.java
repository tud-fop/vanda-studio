package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	
	public Tool(NestedHyperworkflow parent, String name, int id, List<Port> inputPorts, List<Port> outputPorts) {
		this.parent = parent;
		this.name = name;
		this.id = id;
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
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
	}
	
	/** 
	 * Copy constructor that sets the parent of the copy to another NestedHyperworkflow
	 * @param toCopy
	 */
	public Tool(Tool toCopy, NestedHyperworkflow newParent) {
		this(toCopy);
		parent = newParent;
	}
	
	public NestedHyperworkflow getParent() { return parent; }
	public List<Port> getOutputPorts() {	return outputPorts; }
	public int getId() {	return id; }
	public List<Port> getInputPorts() { return inputPorts;	}
	public String getName() { return name; }
	
	@Override
	public boolean equals(Object other) {
		//Tools are equal if they have the same attributes (parent is ignored and not compared)
		boolean result = (other != null && other instanceof Tool);
		if (result) {
			Tool oh = (Tool)other;
			result = (	getId() == oh.getId() &&
					getName().equals(oh.getName()) &&
					getInputPorts().equals(oh.getInputPorts()) &&
					getOutputPorts().equals(oh.getOutputPorts())	);
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
		singletonToolList.add(new Tool(this));
		return singletonToolList;
	}
}
