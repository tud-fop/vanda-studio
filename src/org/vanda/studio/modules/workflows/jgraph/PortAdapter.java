package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.studio.modules.workflows.model.Model;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

class PortAdapter implements Adapter, Cloneable {
	public boolean input;
	public int port;

	public PortAdapter(boolean input, int port) {
		this.input = input;
		this.port = port;
	}
	
	@Override
	public PortAdapter clone() throws CloneNotSupportedException {
		return new PortAdapter(input, port);

	}

	@Override
	public String getName() {
		return "port";
	}

	@Override
	public void onInsert(mxGraph graph, mxICell parent, mxICell cell) {
		
	}

	@Override
	public void onRemove(mxICell parent) {
		
	}

	@Override
	public void setSelection(Model m) {
		
	}

	@Override
	public void register(mxICell parent, mxICell cell) {
		
	}

	@Override
	public boolean inModel() {
		return false;
	}

	@Override
	public void onResize(mxGraph graph, mxICell parent, mxICell cell) {
		// ignore
	}
}