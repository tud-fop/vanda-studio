package org.vanda.workflows.elements;

import org.vanda.util.Repository;
import org.vanda.util.RepositoryItem;

public interface ToolInterface extends RepositoryItem {

	public Repository<? extends Tool> getTools();
	
}
