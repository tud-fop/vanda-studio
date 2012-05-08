package org.vanda.studio.model.workflows;

import java.util.List;

import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;
import org.vanda.studio.model.generation.ArtifactFactory;
import org.vanda.studio.model.generation.Artifacts;
import org.vanda.studio.model.generation.AtomicInvokation;
import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.generation.Profile;
import org.vanda.studio.model.generation.WorkflowElement;

public class IdentityJob<V> extends Job<V> {

	public IdentityJob(WorkflowElement identity) {
		super(identity);
	}

	@Override
	public <T extends ArtifactConn, A extends Artifact<T>, F> AtomicInvokation<T, A> doConvert(
			MemoTable m, ArtifactFactory<T, A, F, V> af, Profile profile) {
		return new AtomicInvokation<T, A>(af.createIdentity(), this);
	}

	@Override
	public boolean isInputPort() {
		return false;
	}

	@Override
	public boolean isOutputPort() {
		return false;
	}

	@Override
	public List<Port> getInputPorts() {
		return Artifacts.identityInputs;
	}

	@Override
	public List<Port> getOutputPorts() {
		return Artifacts.identityOutputs;
	}

}
