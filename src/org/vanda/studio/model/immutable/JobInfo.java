package org.vanda.studio.model.immutable;

import java.util.ArrayList;

public final class JobInfo<F> {
	public final ImmutableJob<F> job;
	public final Object address;
	public final ArrayList<Object> inputs;
	public final ArrayList<Object> outputs;
	public final int outCount;

	/**
	 * Please clone inputs and outputs if they are mutable.
	 * 
	 * @param address
	 * @param inputs
	 * @param outputs
	 * @param outCount
	 */
	public JobInfo(ImmutableJob<F> job, Object address,
			ArrayList<Object> inputs, ArrayList<Object> outputs, int outCount) {
		this.job = job;
		this.address = address;
		this.inputs = inputs;
		this.outputs = outputs;
		this.outCount = outCount;

	}

}
