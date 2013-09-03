package org.vanda.fragment.model;

import org.vanda.workflows.data.DataflowAnalysisEx;

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
