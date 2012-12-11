package org.vanda.studio.app;

import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.elements.Tool;

public interface SemanticsModule extends RepositoryItem {
	
	MetaRepository<Tool> getToolMetaRepository();
	
}
