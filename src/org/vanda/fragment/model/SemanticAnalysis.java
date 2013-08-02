package org.vanda.fragment.model;

import java.util.HashMap;

import org.vanda.util.MultiplexObserver;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.DataflowAnalysis;

public class SemanticAnalysis {
	private DataflowAnalysis dfa;
	private Database db;
	private MultiplexObserver<DataflowAnalysis> dfaChangedObservable;
	
	// FIXME ugly, provisional
	private final boolean executable;

	public SemanticAnalysis(SyntaxAnalysis synA, Database db, boolean executable) {
		this.executable = executable;
		this.db = db;
		dfaChangedObservable = new MultiplexObserver<DataflowAnalysis>();
		updateDFA(synA);
	}
	
	public SemanticAnalysis(SyntaxAnalysis synA, Database db) {
		this(synA, db, false);
	}

	public void updateDFA(SyntaxAnalysis synA) {
		dfa = new DataflowAnalysis(executable);
		HashMap<String, String> assignment = null;
		if (!executable && db.getSize() > 0)
			assignment = db.getRow(db.getCursor());
		if (!executable)
			dfa.init(assignment, db.getCursor(), synA.getSorted());
		else 
			dfa.init(assignment, 0, synA.getSorted());
		dfaChangedObservable.notify(dfa);
	}

	public DataflowAnalysis getDFA() {
		return dfa;
	}
	
	public DataflowAnalysis getDFA(SyntaxAnalysis synA, int i) {
		HashMap<String, String> assignment = null;
		if (i < db.getSize()) {
			assignment = db.getRow(i);
		}
		DataflowAnalysis dfa = new DataflowAnalysis(executable);
		dfa.init(assignment, i, synA.getSorted());
		return dfa;		
		
	}
}
