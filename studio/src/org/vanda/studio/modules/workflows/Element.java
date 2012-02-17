package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author afischer
 */
public class Element extends Hyperworkflow {
	
	private NestedHyperworkflow parent;
	private String name;
	private int id;
	private List<Port> inputPorts;
	private List<Port> outputPorts;
	private Map<Port, Connection> portIncomingConnectionMap;
	
	public Element(NestedHyperworkflow parent, String name, int id, List<Port> inputPorts, List<Port> outputPorts) {
		super(parent, name, id, inputPorts, outputPorts);
//		this.parent = parent;
//		this.name = name;
//		this.id = id;
//		this.inputPorts = inputPorts;
//		this.outputPorts = outputPorts;
//		this.portIncomingConnectionMap = new HashMap<Port, Connection>();
	}
	
	public Element(NestedHyperworkflow parent, String name, int id) {
		this(parent, name, id, new ArrayList<Port>(), new ArrayList<Port>());
	}

	@Override
	public boolean equals(Object other) {
		//FIXME think of something more reasonable to find equal Elements
		//Elements are equal if they have the same id
		boolean result = (other != null && other instanceof Element);
		if (result) {
			Element oh = (Element)other;
			result = (this.getId() == oh.getId());
		}
		return result;
	}
	
	@Override
	public void unfold() {
		//TODO
	}
}
