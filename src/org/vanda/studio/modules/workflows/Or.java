package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vanda.studio.modules.workflows.gui.JGraphRendering.JGraphRendererSelection;

/**
 * 
 * @author afischer
 */
public class Or implements IElement{
	
	//gui stuff
	private double[] dimensions;
	private JGraphRendererSelection renderer;
	public double getX() { return dimensions[0]; }
	public double getY() { return dimensions[1]; }
	public double getWidth() { return dimensions[2]; }
	public double getHeight() { return dimensions[3]; }
	public void selectRenderer(JGraphRendererSelection rs) { this.renderer = rs; }
	public IHyperworkflow clone() { return this; }
	public void setDimensions(double[] dim) { if (dim.length == 4)this.dimensions = dim; }
	//-------------------------------------------------------------------------
	
	private NestedHyperworkflow parent;
	private String name;
	private String id;
	private List<Port> inputPorts;
	private List<Port> outputPorts;
	
	public Or(NestedHyperworkflow parent, String name, List<Port> inputPorts, List<Port> outputPorts) {
		this.parent = parent;
		this.name = name;
		this.id = "0";
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
	}
	
	//creates two inputs and one output by default
	public Or(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
		inputPorts.add(new Port("in"+inputPorts.size(), EPortType.GENERIC));
		inputPorts.add(new Port("in"+inputPorts.size(), EPortType.GENERIC));
		outputPorts.add(new Port("out"+outputPorts.size(), EPortType.GENERIC));
	}
	
	public Or(String name) {
		this(null, name);
	}
	
	/**
	 * Copy constructor
	 * @param toCopy
	 */
	public Or(Or toCopy) {
		this(toCopy.parent, toCopy.name, new ArrayList<Port>(toCopy.inputPorts), new ArrayList<Port>(toCopy.outputPorts));
		this.id = toCopy.getId();
	}
	
	/** 
	 * Copy constructor that sets the parent of the copy to another NestedHyperworkflow
	 * @param toCopy
	 */
	public Or(Or toCopy, NestedHyperworkflow newParent) {
		this(toCopy);
		this.parent = newParent;
	}
	
	public NestedHyperworkflow getParent() { return parent; }
	public void setParent(NestedHyperworkflow newParent) { this.parent = newParent; }
	public List<Port> getOutputPorts() {	return outputPorts; }
	public String getId() {	return id; }
	public boolean setId(String newId) { 
		if (newId != null) {
			id = newId;
			return true;
		}
		return false; 
	}
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
		
		//get incoming and outgoing connections that are connected to current OR node
		List<Connection> incoming = new ArrayList<Connection>();
		List<Connection> outgoing = new ArrayList<Connection>();
		for (Connection c : parent.getConnections()) {
			if (c.getTarget().equals(this)) incoming.add(c);
			if (c.getSource().equals(this)) outgoing.add(c);
		}
		
		for (int i = 0; i < incoming.size(); i++) {
			NestedHyperworkflow parentCopy = new NestedHyperworkflow(parent);	//copy parent NestedHyperworkflow of current or node
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
