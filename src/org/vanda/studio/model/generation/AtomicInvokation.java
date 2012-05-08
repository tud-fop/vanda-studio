package org.vanda.studio.model.generation;

import java.util.List;

public class AtomicInvokation<T extends ArtifactConn, A extends Artifact<T>>
		implements WorkflowElement {
	private final A artifact;
	private final WorkflowElement identity;

	public AtomicInvokation(A artifact, WorkflowElement identity) {
		this.artifact = artifact;
		this.identity = identity;
	}

	public final A getArtifact() {
		return artifact;
	}

	public final Object getIdentity() {
		return identity;
	}

	@Override
	public final List<Port> getInputPorts() {
		return artifact.getInputPorts();
	}

	@Override
	public final List<Port> getOutputPorts() {
		return artifact.getOutputPorts();
	}
}
