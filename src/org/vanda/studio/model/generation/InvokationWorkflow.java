package org.vanda.studio.model.generation;

public class InvokationWorkflow<T extends ArtifactConn, A extends Artifact<T>, F>
		extends Workflow<AtomicInvokation<T, A>, InvokationConnection<T, A>> {

	private final Workflow<?, ?> identity;

	private F fragment;

	public InvokationWorkflow(Workflow<?, ?> identity) {
		this.identity = identity;
	}

	public final Workflow<?, ?> getIdentity() {
		return identity;
	}

	public F getFragment() {
		return fragment;
	}

	public void setFragment(F fragment) {
		this.fragment = fragment;
	}
}