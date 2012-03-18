package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.vanda.studio.modules.workflows.gui.JGraphRendering.JGraphRendererSelection;

/**
 * 
 * @author afischer
 */
public class Tool implements IElement{
	
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
	
	public Tool(NestedHyperworkflow parent, String name, List<Port> inputPorts, List<Port> outputPorts) {
		this.parent = parent;
		this.name = name;
		this.id = "0";
		this.inputPorts = inputPorts;
		this.outputPorts = outputPorts;
	}
	
	public Tool(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	public Tool(String name) {
		this(null, name);
	}
	
	/** 
	 * Copy constructor
	 * @param toCopy
	 */
	public Tool(Tool toCopy) {
		this(toCopy.parent, toCopy.name, new ArrayList<Port>(toCopy.inputPorts), new ArrayList<Port>(toCopy.outputPorts));
		this.id = toCopy.getId();
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
