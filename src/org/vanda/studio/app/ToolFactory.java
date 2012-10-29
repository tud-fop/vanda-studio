package org.vanda.studio.app;

import org.vanda.studio.model.Model;

public interface ToolFactory {
	Object instantiate(WorkflowEditor wfe, Model m);
}
