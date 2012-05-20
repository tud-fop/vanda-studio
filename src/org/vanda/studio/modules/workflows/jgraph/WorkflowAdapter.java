package org.vanda.studio.modules.workflows.jgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.util.TokenSource.Token;

import com.mxgraph.model.mxICell;

public class WorkflowAdapter implements Adapter {
	public final MutableWorkflow<?> workflow;
	public final ArrayList<mxICell> children;
	public final ArrayList<mxICell> connections;
	public final Map<Object, mxICell> inter;
	
	public WorkflowAdapter(MutableWorkflow<?> workflow) {
		this.workflow = workflow;
		children = new ArrayList<mxICell>();
		connections = new ArrayList<mxICell>();
		inter = new HashMap<Object, mxICell>();
	}
	
	public mxICell getChild(Token address) {
		int i = address.intValue();
		if (i < children.size())
			return children.get(i);
		else
			return null;
	}
	
	public mxICell getConnection(Token address) {
		int i = address.intValue();
		if (i < connections.size())
			return connections.get(i);
		else
			return null;
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
	
	public mxICell removeConnection(Token address) {
		int i = address.intValue();
		if (i < connections.size()) {
			mxICell result = connections.get(i);
			connections.set(i, null);
			return result;
		} else
			return null;
	}
	
	public mxICell removeInter(Object j) {
		return inter.remove(j);
	}
	
	public void setChild(Token address, mxICell cell) {
		while (children.size() <= address.intValue())
			children.add(null);
		children.set(address.intValue(), cell);
	}
	
	public void setConnection(Token address, mxICell cell) {
		while (connections.size() <= address.intValue())
			connections.add(null);
		connections.set(address.intValue(), cell);
	}

	@Override
	public String getName() {
		return workflow.toString(); // TODO getName
	}

}
