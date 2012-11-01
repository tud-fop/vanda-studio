package org.vanda.studio.modules.profile;

import java.io.IOException;

import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.profile.model.Fragment;

public interface Profile extends RepositoryItem {
	
	Type getRootType();

	Fragment generate(ImmutableWorkflow iwf) throws IOException;
}
