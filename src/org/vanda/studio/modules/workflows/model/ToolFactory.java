package org.vanda.studio.modules.workflows.model;

import org.vanda.util.RepositoryItem;

public interface ToolFactory extends RepositoryItem {
	Object instantiate(WorkflowEditor wfe);
}
