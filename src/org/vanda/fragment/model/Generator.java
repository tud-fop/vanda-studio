package org.vanda.fragment.model;

import java.io.IOException;

//import org.vanda.execution.model.ExecutableWorkflow;
import org.vanda.types.Type;

public interface Generator {
	
	Type getRootType();

//	Fragment generate(DataflowAnalysis dfa) throws IOException;
//	String generate(ExecutableWorkflow ewf) throws IOException;
	String generate(SyntaxAnalysis synA, SemanticAnalysis semA) throws IOException;
}
