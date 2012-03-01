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
		//Tools are equal if they have the same attributes (parent is ignored and not compared)
		boolean result = (other != null && other instanceof Or);
		if (result) {
			Or oh = (Or)other;
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
		List<IHyperworkflow> hwfList = new ArrayList<IHyperworkflow>();

		//get incoming and outgoing connections
		List<Connection> incoming = new ArrayList<Connection>();
		List<Connection> outgoing = new ArrayList<Connection>();
		for (Connection c : parent.getConnections()) {
			if (c.getTarget().equals(this)) incoming.add(c);
			if (c.getSource().equals(this)) outgoing.add(c);
		}
		
		for (int i = 0; i < incoming.size(); i++) {
			NestedHyperworkflow parentCopy = new NestedHyperworkflow(parent);	//copy parent NestedHyperworkflow of current or node
			parentCopy.removeChild(this);	//remove or node
			
			//connect i-th OR-input with all OR-outputs
			for (int j = 0; j < outgoing.size(); j++) {
				parentCopy.addConnection(new Connection(incoming.get(i).getSource(), incoming.get(i).getSrcPort(), outgoing.get(j).getTarget(), outgoing.get(j).getTargPort()));
			}
			
			//remove the other inputs from the parent NestedHyperworkflow
			//FIXME is this behavior even wanted?! -> number of ports of nested nodes may change due to tool removal
			for (int j = incoming.size() - 1; j >= 0; j--) {
				if (j != i) parentCopy.removeChild(incoming.get(j).getSource());
			}
			
			if (!hwfList.contains(parentCopy)) hwfList.add(parentCopy);		//add unfolded copy to result list
		}
		
		return hwfList;
	}
}
