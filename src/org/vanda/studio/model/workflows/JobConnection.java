package org.vanda.studio.model.workflows;

import org.vanda.studio.model.generation.Connection;

public class JobConnection<V> extends Connection<Job<V>> {

	private final Connection<?> identity;

	public JobConnection(Job<V> source, int sourcePort, Job<V> target,
			int targetPort, Connection<?> identity) {
		super(source, sourcePort, target, targetPort);
		this.identity = identity;
	}

	public Connection<?> getIdentity() {
		return identity;
	}

}
