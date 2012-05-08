package org.vanda.studio.model.generation;

public class Connection<E> {
	private final E source;
	private final int sourcePort;
	private final E target;
	private final int targetPort;

	public Connection(E source, int sourcePort, E target, int targetPort) {
		this.source = source;
		this.sourcePort = sourcePort;
		this.target = target;
		this.targetPort = targetPort;
	}

	public final E getSource() {
		return source;
	}

	public final int getSourcePort() {
		return sourcePort;
	}

	public final E getTarget() {
		return target;
	}

	public final int getTargetPort() {
		return targetPort;
	}
}
