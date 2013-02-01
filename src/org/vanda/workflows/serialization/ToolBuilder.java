package org.vanda.workflows.serialization;

import org.vanda.util.Repository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ElementAdapter;
import org.vanda.workflows.hyper.ToolAdapter;

public class ToolBuilder {
	
	public String id;
	
	public ElementAdapter build(Repository<Tool> tr) {
		return new ToolAdapter(tr.getItem(id));
	}

}
