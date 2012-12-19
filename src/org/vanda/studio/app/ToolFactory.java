package org.vanda.studio.app;

import org.vanda.studio.model.elements.RepositoryItem;

public interface ToolFactory extends RepositoryItem {
	Object instantiate(WorkflowEditor wfe);
}
