package org.vanda.studio.app;

import java.io.IOException;

import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.types.Type;

public interface Profile extends RepositoryItem {
	
	Type getRootType();

	void generate(ImmutableWorkflow iwf) throws IOException;
}
