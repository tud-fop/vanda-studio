package org.vanda.studio.modules.workflows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vanda.studio.model.Action;
import org.vanda.studio.model.Port;
import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.model.VObject;
import org.vanda.studio.model.VObjectInstance;

/**
 * Leaf of Hyperworkflow composite pattern that holds
 * a reference to an actual tool
 * 
 * @author afischer
 */
public class Job extends Hyperworkflow {
	VObject object;
	VObjectInstance instance;

	public Job(VObject o) {
		super(o.getName());
		object = o;
		instance = object.createInstance();
	}

	@Override
	public void appendActions(List<Action> as) {
		instance.appendActions(as);
		object.appendActions(as);
		super.appendActions(as);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		HashMap<String, Object> m = new HashMap<String, Object>();
		instance.saveToMap(m);
		// VOWorkflowObject c = (VOWorkflowObject)super.clone();
		Job c = new Job(object);
		c.instance = object.createInstance();
		c.instance.loadFromMap(m);
		return c;
	}

	@Override
	public List<Port> getInputPorts() {
		List<Port> ports = new ArrayList<Port>();
		for (Port port : object.getInputPorts()) {
			ports.add(port);
		}
		return ports;
	}

	@Override
	public String getName() {
		return object.getName();
	}

	@Override
	public List<Port> getOutputPorts() {
		List<Port> ports = new ArrayList<Port>();
		for (Port port : object.getOutputPorts()) {
			ports.add(port);
		}
		return ports;
	}
	
	@Override
	public void selectRenderer(RendererSelection rs) {
		object.selectRenderer(rs);
	}

	@Override
	public Collection<Hyperworkflow> unfold() {
		return Collections.singletonList((Hyperworkflow)new Job(object));
	}
}