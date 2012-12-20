package org.vanda.studio.modules.profile.model;

import java.io.IOException;

import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.profile.fragments.DataflowAnalysis;
import org.vanda.studio.modules.profile.fragments.Fragment;

public interface Generator {
	
	Type getRootType();

	Fragment generate(DataflowAnalysis dfa) throws IOException;
}
