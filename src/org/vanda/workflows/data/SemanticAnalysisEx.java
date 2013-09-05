package org.vanda.workflows.data;

import org.vanda.workflows.hyper.SyntaxAnalysis;

/**
 * SemanticAnalysis for ExecutableWorkflow
 * @author kgebhardt
 *
 */
public class SemanticAnalysisEx extends SemanticAnalysis {

	public SemanticAnalysisEx(SyntaxAnalysis synA) {
		super(synA, null);
	}

	@Override
	public void updateDFA(SyntaxAnalysis synA) {
		dfa = new DataflowAnalysisEx();
		dfa.init(null, 0, synA.getSorted());
	}

}
