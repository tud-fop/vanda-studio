package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.List;

/** 
 * Leaf of IHyperworkflow composite pattern
 * @author afischer
 */
public abstract class IElement extends IHyperworkflow {
	
	//-------------------------------------------------------------------------
	//----------------------------- constructors ------------------------------
	//-------------------------------------------------------------------------
	
	public IElement(NestedHyperworkflow parent, String name, List<Port> inputPorts, List<Port> outputPorts) {
		super(parent, name, inputPorts, outputPorts);
	}
	
	public IElement(NestedHyperworkflow parent, String name) {
		this(parent, name, new ArrayList<Port>(), new ArrayList<Port>());
	}
	
	public IElement(String name) {
		this(null, name);
	}
	
	/**
	 * Copy constructor - makes a deep copy of the specified IElement except for the parent attribute where only its reference is copied
	 * @param toCopy
	 */
	public IElement(IElement toCopy) {
		this(toCopy.getParent(), toCopy.getName(), new ArrayList<Port>(toCopy.getInputPorts()), new ArrayList<Port>(toCopy.getOutputPorts()));
		setId(toCopy.getId());
	}
	
	public boolean equals(Object other) {
		//Tools are equal if they have the same attributes (parent is ignored and not compared)
		boolean result = (other != null && other.getClass().equals(this.getClass()));
		if (result) {
			IElement oh = (IElement)other;
			result = (	getId() == oh.getId() &&
					getName().equals(oh.getName()) &&
					getInputPorts().equals(oh.getInputPorts()) &&
					getOutputPorts().equals(oh.getOutputPorts())	);
		}
		return result;
	}
}
