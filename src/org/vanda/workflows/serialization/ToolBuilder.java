package org.vanda.workflows.serialization;

import org.vanda.util.Repository;
import org.vanda.workflows.elements.Tool;

public class ToolBuilder {
	
	public String id;
	
	public Tool build(Repository<Tool> tr) {
		return tr.getItem(id);
	}

}
