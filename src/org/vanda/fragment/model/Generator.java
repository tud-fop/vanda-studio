package org.vanda.fragment.model;

import java.io.IOException;

import org.vanda.execution.model.ExecutableWorkflow;
import org.vanda.types.Type;

public interface Generator {
	
	Type getRootType();

//	Fragment generate(DataflowAnalysis dfa) throws IOException;
	Fragment generate(ExecutableWorkflow ewf) throws IOException;

}
