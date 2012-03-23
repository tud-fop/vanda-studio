package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author afischer
 */
public class Or extends IElement {
	
	//gui stuff
	public Object clone() throws CloneNotSupportedException { return new Or(this); }
	
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//----------------------------- constructors ------------------------------
	//-------------------------------------------------------------------------
	
	public Or(NestedHyperworkflow parent, String name, List<Port> inputPorts, List<Port> outputPorts) {
		super(parent, name, inputPorts, outputPorts);
	}
	
	//creates two inputs and one output by default
	public Or(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
		getInputPorts().add(new Port("in"+getInputPorts().size(), EPortType.GENERIC));
		getInputPorts().add(new Port("in"+getInputPorts().size(), EPortType.GENERIC));
		getOutputPorts().add(new Port("out"+getOutputPorts().size(), EPortType.GENERIC));
	}
	
	public Or(String name) {
		this(null, name);
	}
	
	/**
	 * Copy constructor - makes a shallow copy of the specified Or (new instance but attributes reference original attributes)
	 * @param toCopy
	 */
	public Or(Or toCopy) {
		super(toCopy);
	}
	
	/** 
	 * Copy constructor - makes a shallow copy of the specified Or (new instance but attributes reference original attributes)
	 * In addition, the parent of the returned copy is set to another NestedHyperworkflow.
	 * @param toCopy
	 */
	public Or(Or toCopy, NestedHyperworkflow newParent) {
		this(toCopy);
		setParent(newParent);
	}
	
	//-------------------------------------------------------------------------
	//--------------------------- functionality -------------------------------
	//-------------------------------------------------------------------------
	
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
	public Collection<IHyperworkflow> unfold() {
		List<IHyperworkflow> hwfList = new ArrayList<IHyperworkflow>();
		
		//get incoming and outgoing connections that are connected to current OR node
		List<Connection> incoming = new ArrayList<Connection>();
		List<Connection> outgoing = new ArrayList<Connection>();
		for (Connection c : getParent().getConnections()) {
			if (c.getTarget().equals(this)) incoming.add(c);
			if (c.getSource().equals(this)) outgoing.add(c);
		}
		
		for (int i = 0; i < incoming.size(); i++) {
			NestedHyperworkflow parentCopy = new NestedHyperworkflow(getParent());	//copy parent NestedHyperworkflow of current or node
			parentCopy.removeChild(this, false);	//remove or node
			
			//connect i-th OR-input with all OR-outputs
			for (int j = 0; j < outgoing.size(); j++) {
				parentCopy.addConnection(new Connection(incoming.get(i).getSource(), incoming.get(i).getSrcPort(), outgoing.get(j).getTarget(), outgoing.get(j).getTargPort()));
			}
			
			//remove the other inputs from the parent NestedHyperworkflow
			//FIXME is this behavior even wanted?! -> number of ports of nested nodes may change due to tool removal
			for (int j = incoming.size() - 1; j >= 0; j--) {
				if (j != i) parentCopy.removeChild(incoming.get(j).getSource(), true);
			}
			
			if (!hwfList.contains(parentCopy)) hwfList.add(parentCopy);		//add unfolded copy to result list
		}
		
		return hwfList;
	}
}
