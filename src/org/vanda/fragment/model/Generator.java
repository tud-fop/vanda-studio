package org.vanda.fragment.model;

import java.io.IOException;



//import org.vanda.execution.model.ExecutableWorkflow;
import org.vanda.types.Type;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public interface Generator {
	
	Type getRootType();

//	Fragment generate(DataflowAnalysis dfa) throws IOException;
//	String generate(ExecutableWorkflow ewf) throws IOException;
	String generate(MutableWorkflow ewf, SyntaxAnalysis synA, SemanticAnalysis semA) throws IOException;
}
