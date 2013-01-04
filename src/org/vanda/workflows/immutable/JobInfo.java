package org.vanda.workflows.immutable;

import java.util.ArrayList;

import org.vanda.util.TokenSource.Token;

public final class JobInfo {
	public final ImmutableJob job;
	public final Token address;
	public final ArrayList<Token> inputs;
	public final ArrayList<Token> outputs;
	public final int outCount;
	public final boolean connected;

	/**
	 * Please clone inputs and outputs if they are mutable.
	 * 
	 * @param address
	 * @param inputs
	 * @param outputs
	 * @param outCount
	 */
	public JobInfo(ImmutableJob job, Token address,
			ArrayList<Token> inputs, ArrayList<Token> outputs, int outCount,
			boolean connected) {
		this.job = job;
		this.address = address;
		this.inputs = inputs;
		this.outputs = outputs;
		this.outCount = outCount;
		this.connected = connected;

	}

}
