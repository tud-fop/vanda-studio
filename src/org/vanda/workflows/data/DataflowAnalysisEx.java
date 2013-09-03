package org.vanda.workflows.data;

import java.util.HashMap;
import java.util.Map;

import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Jobs;
import org.vanda.workflows.hyper.Location;

public class DataflowAnalysisEx extends DataflowAnalysis {
	@Override
	public void init(Map<String, String> assignment, int line, Job[] sorted) {
		assignment_ = assignment;
		connected = true;
		this.line = line;
		jobIds = new HashMap<Job, String>();
		jobSpecs = new HashMap<Job, String>();
		values = new HashMap<Location, String>();
		if (sorted == null)
			return;
		Jobs.visitAll(sorted, this);
	}
	
	@Override
	public void visitLiteral(Job j, Literal l) {
		String value = l.getKey();		
		values.put(j.bindings.get(j.getOutputPorts().get(0)), value);
	}
	
	@Override
	protected String computeJobId(Job j, Tool t) {
		return j.getId();
	}
	
}
