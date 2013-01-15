package org.vanda.workflows.toolinterfaces;

public interface Parser {
	ToolBuilder getToolBuilder();
	ToolInterfaceBuilder getToolInterfaceBuilder();
	void buildTool();
	void buildToolInterface();
}
