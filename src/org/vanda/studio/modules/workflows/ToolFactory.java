package org.vanda.studio.modules.workflows;

public interface ToolFactory {
	Object instantiate(WorkflowEditor wfe, Model<?> m);
}
