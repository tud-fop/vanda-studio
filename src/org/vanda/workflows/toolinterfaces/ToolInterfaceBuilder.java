package org.vanda.workflows.toolinterfaces;

import java.util.LinkedList;
import java.util.List;

class ToolInterfaceBuilder extends RepositoryItemBuilder {

	LinkedList<ToolBuilder> tools;

	public ToolInterfaceBuilder() {
		reset();
	}

	public void reset() {
		super.reset();
		tools = new LinkedList<ToolBuilder>();
	}

	public List<StaticTool> build() {
		LinkedList<StaticTool> result = new LinkedList<StaticTool>();
		StaticToolInterface ti = new StaticToolInterface(id, name,
				description.toString(), version, category, contact);
		for (ToolBuilder tb : tools) {
			tb.ti = ti;
			result.add(tb.build());
		}
		return result;
	}

}
