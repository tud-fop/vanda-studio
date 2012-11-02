package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.studio.model.Model;
import org.vanda.studio.model.Model.VariableSelection;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.util.TokenSource.Token;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class LocationAdapter implements Adapter, Cloneable {
	
	private final int index;
	private Token address;

	public LocationAdapter(int index, Token address) {
		this.index = index;
		this.address = address;
	}

	@Override
	public LocationAdapter clone() throws CloneNotSupportedException {
		return new LocationAdapter(index, address);

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
		m.setSelection(new VariableSelection(m.getRoot(), address));
		// TODO no nesting support here because of m.getRoot()
	}
	
	public void updateLocation(Job job) {
		address = job.outputs.get(index);
		// System.err.println(address.intValue());
	}

}
