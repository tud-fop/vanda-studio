package org.vanda.studio.app;

import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.types.Type;

public interface Profile extends RepositoryItem {
	
	Generator createGenerator();
	
	Type getRootType();
}
