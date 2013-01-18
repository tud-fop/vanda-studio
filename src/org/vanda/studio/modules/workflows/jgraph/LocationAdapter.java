package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.studio.modules.workflows.model.Model;
import org.vanda.studio.modules.workflows.model.Model.VariableSelection;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class LocationAdapter implements Adapter, Cloneable {
	
	private final Port port;
	private Location variable;

	public LocationAdapter(Port port, Location variable) {
		this.port = port;
		this.variable = variable;
	}

	@Override
	public LocationAdapter clone() throws CloneNotSupportedException {
		return new LocationAdapter(port, variable);
	}

	@Override
	public String getName() {
		return "location";
	}

	@Override
	public boolean inModel() {
		return false;
	}

	@Override
	public void onInsert(mxGraph graph, mxICell parent, mxICell cell) {
	}

	@Override
	public void onRemove(mxICell parent) {

	}

	@Override
	public void onResize(mxGraph graph, mxICell parent, mxICell cell) {

	}

	@Override
	public void register(mxICell parent, mxICell cell) {

	}

	@Override
	public void setSelection(Model m) {
		m.setSelection(new VariableSelection(m.getRoot(), variable));
		// TODO no nesting support here because of m.getRoot()
	}
	
	public void updateLocation(Job job) {
		variable = job.bindings.get(port);
		// System.err.println(address.intValue());
	}

}
