package org.vanda.studio.model.generation;

import java.util.List;

public interface WorkflowElement {
	public List<Port> getInputPorts();

	public List<Port> getOutputPorts();

}
