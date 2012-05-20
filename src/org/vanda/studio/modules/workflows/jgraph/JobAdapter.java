package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.studio.model.hyper.Job;

public class JobAdapter<F> implements Adapter, Cloneable {
	public final Job<F> job;
	
	public JobAdapter(Job<F> job) {
		this.job = job;
	}
	
	@Override
	public JobAdapter<F> clone() throws CloneNotSupportedException {
		return new JobAdapter<F>(job.clone());
		
	}

	@Override
	public String getName() {
		return job.getName();
	}
}
