package org.vanda.studio.modules.workflows.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.vanda.studio.model.Action;
import org.vanda.studio.model.RendererSelection;
import org.vanda.studio.model.VObject;
import org.vanda.studio.model.VObjectInstance;
import org.vanda.studio.modules.workflows.Hyperworkflow;
import org.vanda.studio.modules.workflows.Port;

//XXX oder doch von Tool erben lassen?
public class VOWorkflowObject extends Hyperworkflow {
	VObject object;
	VObjectInstance instance;

	public VOWorkflowObject(VObject o) {
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
		VOWorkflowObject c = new VOWorkflowObject(object);
		c.instance = object.createInstance();
		c.instance.loadFromMap(m);
		return c;
	}

	@Override
	public List<Port> getInputPorts() {
		List<Port> ports = new ArrayList<Port>();
		for (String port : object.getInputPorts()) {
			ports.add(new Port(port, "type"));
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
		for (String port : object.getOutputPorts()) {
			ports.add(new Port(port, "type"));
		}
		return ports;
	}

	@Override
	public void selectRenderer(RendererSelection rs) {
		object.selectRenderer(rs);
	}

	@Override
	public Collection<Hyperworkflow> unfold() {
		List<Hyperworkflow> singletonToolList = new ArrayList<Hyperworkflow>();
		singletonToolList.add(new VOWorkflowObject(object));
		return singletonToolList;
	}
}