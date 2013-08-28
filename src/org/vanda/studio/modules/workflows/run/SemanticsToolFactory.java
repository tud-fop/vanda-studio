package org.vanda.studio.modules.workflows.run;

//import org.vanda.fragment.model.Model;
import org.vanda.fragment.model.SemanticAnalysis;
import org.vanda.fragment.model.SyntaxAnalysis;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.view.View;

public interface SemanticsToolFactory {
//	Object instantiate(WorkflowEditor wfe, Model model, View view);
	Object instantiate(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA, View view);
}
