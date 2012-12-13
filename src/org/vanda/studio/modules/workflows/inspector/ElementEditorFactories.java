package org.vanda.studio.modules.workflows.inspector;

import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.OutputPort;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.MutableWorkflow;

public class ElementEditorFactories {
	
	public final CompositeFactory<Connection> connectionFactories;
	
	public final CompositeFactory<InputPort> inputPortFactories;
	
	public final CompositeFactory<Literal> literalFactories;
	
	public final CompositeFactory<OutputPort> outputPortFactories;
	
	public final CompositeFactory<Tool> toolFactories;
	
	public final CompositeFactory<MutableWorkflow> variableFactories;
	
	public final CompositeFactory<MutableWorkflow> workflowFactories;
	
	{
		connectionFactories = new CompositeFactory<Connection>();
		inputPortFactories = new CompositeFactory<InputPort>();
		literalFactories = new CompositeFactory<Literal>();
		outputPortFactories = new CompositeFactory<OutputPort>();
		toolFactories = new CompositeFactory<Tool>();
		variableFactories = new CompositeFactory<MutableWorkflow>();
		workflowFactories = new CompositeFactory<MutableWorkflow>();
	}

}
