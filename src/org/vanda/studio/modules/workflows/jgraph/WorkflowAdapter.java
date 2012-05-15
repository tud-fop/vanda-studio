package org.vanda.studio.modules.workflows.jgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.util.TokenSource.Token;

import com.mxgraph.model.mxICell;

public class WorkflowAdapter {
	public final MutableWorkflow<?> workflow;
	public final ArrayList<mxICell> translation;
	public final Map<Job<?>, mxICell> inter;
	
	public WorkflowAdapter(MutableWorkflow<?> workflow) {
		this.workflow = workflow;
		translation = new ArrayList<mxICell>();
		inter = new HashMap<Job<?>, mxICell>();
	}
	
	public mxICell get(Token address) {
		int i = address.intValue();
		if (i < translation.size())
			return translation.get(i);
		else
			return null;
	}
	
	public mxICell getInter(Job<?> j) {
		return inter.get(j);
	}
	
	public void putInter(Job<?> j, mxICell c) {
		inter.put(j, c);
	}
	
	public mxICell remove(Token address) {
		int i = address.intValue();
		if (i < translation.size()) {
			mxICell result = translation.get(i);
			translation.set(i, null);
			return result;
		} else
			return null;
	}
	
	public void removeInter(Job<?> j) {
		inter.remove(j);
	}
	
	public void set(Token address, mxICell cell) {
		while (translation.size() <= address.intValue())
			translation.add(null);
		translation.set(address.intValue(), cell);
	}

}
