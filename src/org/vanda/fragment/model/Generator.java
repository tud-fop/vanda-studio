package org.vanda.fragment.model;

import java.io.IOException;

import org.vanda.types.Type;

public interface Generator {
	
	Type getRootType();

	Fragment generate(DataflowAnalysis dfa) throws IOException;
}
