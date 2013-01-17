package org.vanda.studio.modules.workflows.inspector;

import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.MutableWorkflow;

public class ElementEditorFactories {
	
	public final CompositeFactory<ConnectionKey> connectionFactories;
	
	public final CompositeFactory<Literal> literalFactories;
	
	public final CompositeFactory<Tool> toolFactories;
	
	public final CompositeFactory<MutableWorkflow> variableFactories;
	
	public final CompositeFactory<MutableWorkflow> workflowFactories;
	
	{
		connectionFactories = new CompositeFactory<ConnectionKey>();
		literalFactories = new CompositeFactory<Literal>();
		toolFactories = new CompositeFactory<Tool>();
		variableFactories = new CompositeFactory<MutableWorkflow>();
		workflowFactories = new CompositeFactory<MutableWorkflow>();
	}

}
