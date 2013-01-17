package org.vanda.studio.modules.workflows.jgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.vanda.studio.modules.workflows.model.Model;
import org.vanda.studio.modules.workflows.model.Model.WorkflowSelection;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.MutableWorkflow;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class WorkflowAdapter implements Adapter {
	public final MutableWorkflow workflow;
	public final ArrayList<mxICell> children;
	public final Map<ConnectionKey, mxICell> connections;
	public final Map<Object, mxICell> inter;
	
	public WorkflowAdapter(MutableWorkflow workflow) {
		this.workflow = workflow;
		children = new ArrayList<mxICell>();
		connections = new HashMap<ConnectionKey, mxICell>();
		inter = new HashMap<Object, mxICell>();
	}
	
	@Override
	public WorkflowAdapter clone() {
		// TODO this is highly bogus!
		return new WorkflowAdapter(workflow);
	}
	
	public mxICell getChild(Token address) {
		int i = address.intValue();
		if (i < children.size())
			return children.get(i);
		else
			return null;
	}
	
	public mxICell getConnection(ConnectionKey cc) {
		return connections.get(cc);
	}
	
	public void putInter(Object j, mxICell c) {
		inter.put(j, c);
	}
	
	public mxICell removeChild(Token address) {
		int i = address.intValue();
		if (i < children.size()) {
			mxICell result = children.get(i);
			children.set(i, null);
			return result;
		} else
			return null;
	}
	
	public mxICell removeConnection(ConnectionKey cc) {
		return connections.remove(cc);
	}
	
	public mxICell removeInter(Object j) {
		return inter.remove(j);
	}
	
	public void setChild(Token address, mxICell cell) {
		while (children.size() <= address.intValue())
			children.add(null);
		children.set(address.intValue(), cell);
	}
	
	public void setConnection(ConnectionKey cc, mxICell cell) {
		connections.put(cc, cell);
	}

	@Override
	public String getName() {
		return workflow.getName();
	}

	@Override
	public void onRemove(mxICell parent) {
		System.out.println("Curious thing just happened!");
	}

	@Override
	public void onInsert(mxGraph graph, mxICell parent, mxICell cell) {
		// do nothing
	}

	@Override
	public void setSelection(Model m) {
		m.setSelection(new WorkflowSelection(workflow));
	}

	@Override
	public void register(mxICell parent, mxICell cell) {
		// do nothing
		
	}

	@Override
	public boolean inModel() {
		// XXX not sure what to return here
		return false;
	}

	@Override
	public void onResize(mxGraph graph, mxICell parent, mxICell cell) {
		// ignore XXX could check whether parent needs to be resized...
		
		// resize port children as well
		for (int i = 0; i < cell.getChildCount(); i++) {
			if (cell.getChildAt(i).getValue() instanceof OutputPortAdapter) {
				((OutputPortAdapter)cell.getChildAt(i).getValue()).onResize(graph, cell, cell.getChildAt(i));
			}
			if (cell.getChildAt(i).getValue() instanceof InputPortAdapter) {
				((InputPortAdapter)cell.getChildAt(i).getValue()).onResize(graph, cell, cell.getChildAt(i));
			}
		}
	}

}
