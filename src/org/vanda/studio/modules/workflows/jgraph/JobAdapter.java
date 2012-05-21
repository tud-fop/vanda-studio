package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.studio.model.hyper.Job;

public class JobAdapter implements Adapter, Cloneable {
	public final Job job;
	
	public JobAdapter(Job job) {
		this.job = job;
	}
	
	@Override
	public JobAdapter clone() throws CloneNotSupportedException {
		return new JobAdapter(job.clone());
		
	}

	@Override
	public String getName() {
		return job.getName();
	}
}
