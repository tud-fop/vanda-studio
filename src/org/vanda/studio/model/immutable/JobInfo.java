package org.vanda.studio.model.immutable;

import java.util.ArrayList;

import org.vanda.studio.util.TokenSource.Token;

public final class JobInfo<F> {
	public final ImmutableJob<F> job;
	public final Object address;
	public final ArrayList<Token> inputs;
	public final ArrayList<Token> outputs;
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
			ArrayList<Token> inputs, ArrayList<Token> outputs, int outCount) {
		this.job = job;
		this.address = address;
		this.inputs = inputs;
		this.outputs = outputs;
		this.outCount = outCount;

	}

}