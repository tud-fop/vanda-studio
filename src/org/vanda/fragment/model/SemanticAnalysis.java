package org.vanda.fragment.model;

import org.vanda.util.MultiplexObserver;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.DataflowAnalysis;

public class SemanticAnalysis {
	private DataflowAnalysis dfa;
	private Database db;
	private MultiplexObserver<DataflowAnalysis> dfaChangedObservable;

	public SemanticAnalysis(SyntaxAnalysis synA, Database db) {
		this.db = db;
		dfaChangedObservable = new MultiplexObserver<DataflowAnalysis>();
		updateDFA(synA);
	}

	public void updateDFA(SyntaxAnalysis synA) {
		dfa = new DataflowAnalysis();
		if (db.getSize() > 0) {
			dfa.init(db.getRow(db.getCursor()), synA.getSorted());
			dfaChangedObservable.notify(dfa);
		}
	}

	public DataflowAnalysis getDFA() {
		return dfa;
	}
}
