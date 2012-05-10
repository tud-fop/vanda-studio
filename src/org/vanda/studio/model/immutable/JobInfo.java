package org.vanda.studio.model.immutable;

import java.util.ArrayList;

public final class JobInfo<F> {
	public final ImmutableJob<F> job;
	public final Integer address;
	public final ArrayList<Integer> inputs;
	public final ArrayList<Integer> outputs;
	public final int outCount;

	/**
	 * Please clone inputs and outputs if they are mutable.
	 * 
	 * @param address
	 * @param inputs
	 * @param outputs
	 * @param outCount
	 */
	public JobInfo(ImmutableJob<F> job, Integer address,
			ArrayList<Integer> inputs, ArrayList<Integer> outputs, int outCount) {
		this.job = job;
		this.address = address;
		this.inputs = inputs;
		this.outputs = outputs;
		this.outCount = outCount;

	}

}
