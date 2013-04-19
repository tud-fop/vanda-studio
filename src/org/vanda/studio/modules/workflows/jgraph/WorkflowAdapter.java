package org.vanda.studio.modules.workflows.jgraph;

import java.util.HashMap;
import java.util.Map;

import org.vanda.studio.modules.workflows.model.Model;
//import org.vanda.studio.modules.workflows.model.Model.WorkflowSelection;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public class WorkflowAdapter implements Adapter {
	public final MutableWorkflow workflow;
	public final Map<Job, mxICell> children;
	public final Map<ConnectionKey, mxICell> connections;
	public final Map<Object, mxICell> inter;
	
	public WorkflowAdapter(MutableWorkflow workflow) {
		this.workflow = workflow;
		children = new HashMap<Job, mxICell>();
		connections = new HashMap<ConnectionKey, mxICell>();
		inter = new HashMap<Object, mxICell>();
	}
	
	@Override
	public WorkflowAdapter clone() {
		// TODO this is highly bogus!
		return new WorkflowAdapter(workflow);
	}
	
	public mxICell getChild(Job job) {
		return children.get(job);
	}
	
	public mxICell getConnection(ConnectionKey cc) {
		return connections.get(cc);
	}
	
	public mxICell removeChild(Job job) {
		return children.remove(job);
	}
	
	public mxICell removeConnection(ConnectionKey cc) {
		return connections.remove(cc);
	}
	
	public void setChild(Job job, mxICell cell) {
		children.put(job, cell);
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
//		m.setSelection(new WorkflowSelection(workflow));
		
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
	}

}
