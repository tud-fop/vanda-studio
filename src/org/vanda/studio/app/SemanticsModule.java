package org.vanda.studio.app;

import org.vanda.util.MetaRepository;
import org.vanda.util.RepositoryItem;
import org.vanda.workflows.elements.Tool;

public interface SemanticsModule extends RepositoryItem {
	
	MetaRepository<Tool> getToolMetaRepository();
	
}
