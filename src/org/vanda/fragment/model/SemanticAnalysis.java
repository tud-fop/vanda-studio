package org.vanda.fragment.model;

import java.util.HashMap;

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
		HashMap<String, String> assignment = null;
		if (db.getSize() > 0)
			assignment = db.getRow(db.getCursor());
		dfa.init(assignment, synA.getSorted());
		dfaChangedObservable.notify(dfa);
	}

	public DataflowAnalysis getDFA() {
		return dfa;
	}
}
