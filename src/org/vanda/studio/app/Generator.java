package org.vanda.studio.app;

import org.vanda.studio.model.immutable.ImmutableWorkflow;

public interface Generator {
	
	void generate(ImmutableWorkflow iwf);
	
}
