package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author afischer
 *
 */
public class Or implements IElement{

	private NestedHyperworkflow parent;
	private String name;
	private int id;
	private List<Port> inputPorts;
	private List<Port> outputPorts;
	
	public Or(NestedHyperworkflow parent, String name, int id, List<Port> inputPorts, List<Port> outputPorts) {
		this.parent = parent;
		this.name = name;
		this.id = id;
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
	}
	
	//creates two inputs and one output by default
	public Or(NestedHyperworkflow parent, String name, int id) {
		this(parent, name, id, new ArrayList<Port>(), new ArrayList<Port>());
		inputPorts.add(new Port("in"+inputPorts.size(), EPortType.GENERIC));
		inputPorts.add(new Port("in"+inputPorts.size(), EPortType.GENERIC));
		outputPorts.add(new Port("out"+outputPorts.size(), EPortType.GENERIC));
	}
	
	public NestedHyperworkflow getParent() { return parent; }
	public List<Port> getOutputPorts() {	return outputPorts; }
	public int getId() {	return id; }
	public List<Port> getInputPorts() { return inputPorts;	}
	public String getName() { return name; }
	
	@Override
	public boolean equals(Object other) {
		//FIXME think of something more reasonable to find equal Tools
		//Tools are equal if they have the same id
		boolean result = (other != null && other instanceof Or);
		if (result) {
			Or oh = (Or)other;
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
		List<IHyperworkflow> hwfList = new ArrayList<IHyperworkflow>();

		for (int i = 0; i < inputPorts.size(); i++) {
			NestedHyperworkflow parentCopy = new NestedHyperworkflow(parent);	//copy parent NestedHyperworkflow of current or node
			parentCopy.removeChild(this);	//remove or node
			
			// add connection between or-input and or-output
			//TODO get incoming connection
			//TODO get outgoing connections
			
			hwfList.add(parentCopy);		//add unfolded copy to result list
			
		}
		
		return hwfList;
	}
}
