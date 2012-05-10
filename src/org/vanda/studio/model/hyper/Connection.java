package org.vanda.studio.model.hyper;

public class Connection<F> {
	private final Job<F> source;
	private final int sourcePort;
	private final Job<F> target;
	private final int targetPort;

	public Connection(Job<F> source, int sourcePort, Job<F> target, int targetPort) {
		this.source = source;
		this.sourcePort = sourcePort;
		this.target = target;
		this.targetPort = targetPort;
	}

	public final Job<F> getSource() {
		return source;
	}

	public final int getSourcePort() {
		return sourcePort;
	}

	public final Job<F> getTarget() {
		return target;
	}

	public final int getTargetPort() {
		return targetPort;
	}
}
