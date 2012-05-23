package org.vanda.studio.app;

import java.io.IOException;

import org.vanda.studio.model.immutable.ImmutableWorkflow;

public interface Generator {
	
	void generate(ImmutableWorkflow iwf) throws IOException;
	
}
