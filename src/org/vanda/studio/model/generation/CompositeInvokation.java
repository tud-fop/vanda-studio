package org.vanda.studio.model.generation;

public final class CompositeInvokation<T extends ArtifactConn, A extends Artifact<T>, IF>
		extends AtomicInvokation<T, A> {
	private final InvokationWorkflow<?, ?, IF> workflow;

	public CompositeInvokation(InvokationWorkflow<?, ?, IF> workflow,
			A artifact, WorkflowElement identity) {
		super(artifact, identity);
		this.workflow = workflow;
	}

	public InvokationWorkflow<?, ?, IF> getWorkflow() {
		return workflow;
	}
}
