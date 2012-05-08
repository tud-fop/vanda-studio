package org.vanda.studio.app;

import org.vanda.studio.model.hyper.HyperWorkflow;

public interface WorkflowDescription {
	
	String getAuthor();
	
	String getName();
	
	HyperWorkflow<?, ?> load();
}
