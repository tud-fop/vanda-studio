package org.vanda.studio.model.workflows;

import java.util.List;

import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;
import org.vanda.studio.model.generation.ArtifactFactory;
import org.vanda.studio.model.generation.AtomicInvokation;
import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.generation.Profile;
import org.vanda.studio.model.generation.WorkflowElement;

public final class AtomicJob<V, I extends ToolInstance> extends Job<V> {
	private final Tool<V, I> tool;

	private final I toolInstance;

	public AtomicJob(Tool<V, I> tool, I toolInstance, WorkflowElement identity) {
		super(identity);
		this.tool = tool;
		this.toolInstance = toolInstance;
	}

	@Override
	public <T extends ArtifactConn, A extends Artifact<T>, F> AtomicInvokation<T, A> doConvert(
			MemoTable m, ArtifactFactory<T, A, F, V> af, Profile profile) {
		return new AtomicInvokation<T, A>(
				tool.createArtifact(af, toolInstance), this);
	}

	@Override
	public List<Port> getInputPorts() {
		return tool.getInputPorts();
	}

	@Override
	public List<Port> getOutputPorts() {
		return tool.getOutputPorts();
	}

	@Override
	public boolean isInputPort() {
		return tool instanceof InputPort;
	}

	@Override
	public boolean isOutputPort() {
		return tool instanceof OutputPort;
	}
}
