package org.vanda.studio.model.workflows;

import java.util.List;

import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;
import org.vanda.studio.model.generation.ArtifactFactory;
import org.vanda.studio.model.generation.AtomicInvokation;
import org.vanda.studio.model.generation.CompositeInvokation;
import org.vanda.studio.model.generation.InvokationWorkflow;
import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.generation.Profile;
import org.vanda.studio.model.generation.WorkflowElement;

public final class CompositeJob<IF, V, IV, I extends ToolInstance> extends Job<V> {
	
	private I instance;

	private final Linker<IF, V, I> linker;

	private final JobWorkflow<IF, IV> workflow;

	public CompositeJob(Linker<IF, V, I> linker, I instance, JobWorkflow<IF, IV> workflow,
			WorkflowElement identity) {
		super(identity);
		this.linker = linker;
		this.instance = instance;
		this.workflow = workflow;
	}

	@Override
	public <T extends ArtifactConn, A extends Artifact<T>, F> AtomicInvokation<T, A> doConvert(
			MemoTable m, ArtifactFactory<T, A, F, V> af, Profile profile) {
		InvokationWorkflow<?, ?, IF> iwf = workflow.convert(m, profile);
		A a = linker.link(af, iwf, instance);
		return new CompositeInvokation<T, A, IF>(iwf, a, this);
	}

	@Override
	public List<Port> getInputPorts() {
		return workflow.getInputPorts();
	}

	@Override
	public List<Port> getOutputPorts() {
		return workflow.getOutputPorts();
	}

	@Override
	public boolean isInputPort() {
		return false;
	}

	@Override
	public boolean isOutputPort() {
		return false;
	}

}
