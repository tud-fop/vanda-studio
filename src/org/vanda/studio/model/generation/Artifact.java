package org.vanda.studio.model.generation;

import java.util.List;

import org.vanda.studio.util.HasActions;

public interface Artifact<T extends ArtifactConn> extends HasActions {
	// get some informative string

	List<Port> getInputPorts();

	List<Port> getOutputPorts();

	/**
	 * Compute output ArtifactConnections for the given input
	 * ArtifactConnections.
	 * <p>
	 * Precondition: inputs.size() == getInputPorts().size(), inputs does not
	 * contain null
	 * <p>
	 * Postcondition: getOutputs(inputs).size() == getOutputPorts().size(),
	 * getOutputs(inputs) does not contain null
	 * 
	 * @param inputs
	 * @return
	 */
	List<T> getOutputs(List<T> inputs);
}
