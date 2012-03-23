package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author afischer
 */
public class Tool extends IElement{
	
	//gui stuff
	public Object clone() throws CloneNotSupportedException { return new Tool(this); }
	
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//----------------------------- constructors ------------------------------
	//-------------------------------------------------------------------------
	
	public Tool(NestedHyperworkflow parent, String name, List<Port> inputPorts, List<Port> outputPorts) {
		super(parent, name, inputPorts, outputPorts);
	}
	
	public Tool(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	public Tool(String name) {
		this(null, name);
	}
	
	/** 
	 * Copy constructor - makes a shallow copy of the specified Tool (new instance but attributes reference original attributes)
	 * @param toCopy
	 */
	public Tool(Tool toCopy) {
		super(toCopy);
	}

	//-------------------------------------------------------------------------
	//--------------------------- functionality -------------------------------
	//-------------------------------------------------------------------------
	
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
	public Collection<IHyperworkflow> unfold() {
		List<IHyperworkflow> singletonToolList = new ArrayList<IHyperworkflow>();
		singletonToolList.add(new Tool(this));
		return singletonToolList;
	}
}
