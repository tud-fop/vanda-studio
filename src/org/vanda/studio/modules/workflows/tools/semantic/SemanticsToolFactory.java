package org.vanda.studio.modules.workflows.tools.semantic;

//import org.vanda.fragment.model.Model;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.view.View;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public interface SemanticsToolFactory {
//	Object instantiate(WorkflowEditor wfe, Model model, View view);
	Object instantiate(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA, View view);
}
