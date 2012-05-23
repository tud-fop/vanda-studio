package org.vanda.studio.modules.workflows.jgraph;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.modules.workflows.Model;
import org.vanda.studio.util.TokenSource.Token;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

class PortAdapter implements Adapter, Cloneable {
	public boolean input;
	public Port port;

	public PortAdapter(boolean input, Port port) {
		this.input = input;
		this.port = port;
	}

	@Override
	public String getName() {
		return "port";
	}

	@Override
	public void update(mxGraph graph, mxICell parent, mxICell cell) {
		
	}

	@Override
	public void remove(mxICell parent) {
		
	}

	@Override
	public void prependPath(LinkedList<Token> path) {
		
	}

	@Override
	public void setSelection(Model m, List<Token> path) {
		
	}

	@Override
	public void register(mxICell parent, mxICell cell) {
		
	}

	@Override
	public mxICell dereference(ListIterator<Token> path, mxICell current) {
		return null;
	}

	@Override
	public boolean inModel() {
		return false;
	}
}