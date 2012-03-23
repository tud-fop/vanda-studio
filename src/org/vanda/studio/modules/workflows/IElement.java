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
	 * Copy constructor - makes a shallow copy of the specified IElement (new instance but attributes reference original attributes)
	 * @param toCopy
	 */
	public IElement(IElement toCopy) {
		this(toCopy.getParent(), toCopy.getName(), new ArrayList<Port>(toCopy.getInputPorts()), new ArrayList<Port>(toCopy.getOutputPorts()));
		setId(toCopy.getId());
	}
}
