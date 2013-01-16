package org.vanda.workflows.toolinterfaces;

import java.util.LinkedList;

import org.vanda.util.ListRepository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.elements.ToolInterface;

class ToolInterfaceBuilder extends RepositoryItemBuilder {

	LinkedList<ToolBuilder> tools;

	public ToolInterfaceBuilder() {
		reset();
	}

	public void reset() {
		super.reset();
		tools = new LinkedList<ToolBuilder>();
	}

	public ToolInterface build() {
		StaticToolInterface result = new StaticToolInterface(id, name,
				description.toString(), version, category, contact);
		ListRepository<Tool> lr = result.getRepository();
		for (ToolBuilder tb : tools) {
			tb.ti = result;
			lr.addItem(tb.build());
		}
		return result;
	}

}
