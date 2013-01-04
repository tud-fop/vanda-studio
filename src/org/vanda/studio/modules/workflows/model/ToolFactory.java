package org.vanda.studio.modules.workflows.model;

import org.vanda.workflows.elements.RepositoryItem;

public interface ToolFactory extends RepositoryItem {
	Object instantiate(WorkflowEditor wfe);
}
