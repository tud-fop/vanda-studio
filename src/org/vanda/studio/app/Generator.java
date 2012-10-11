package org.vanda.studio.app;

import java.io.IOException;

import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.modules.profile.model.Fragment;

public interface Generator {
	
	Fragment generate(ImmutableWorkflow iwf) throws IOException;
	
}
