package org.vanda.workflows.toolinterfaces;

import java.util.LinkedList;
import java.util.List;

import org.vanda.workflows.elements.Tool;

class ToolInterfaceBuilder extends RepositoryItemBuilder {

	LinkedList<ToolBuilder> tools;

	public ToolInterfaceBuilder() {
		reset();
	}

	public void reset() {
		super.reset();
		tools = new LinkedList<ToolBuilder>();
	}

	public List<Tool> build() {
		LinkedList<Tool> result = new LinkedList<Tool>();
		StaticToolInterface ti = new StaticToolInterface(id, name,
				description.toString(), version, category, contact);
		for (ToolBuilder tb : tools) {
			tb.ti = ti;
			result.add(tb.build());
		}
		return result;
	}

}
