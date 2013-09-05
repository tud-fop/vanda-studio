package org.vanda.workflows.data;

import java.util.HashMap;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.workflows.hyper.SyntaxAnalysis;

/**
 * Holds and updates the DataflowAnalysis of a Workflow.
 * @author kgebhardt
 *
 */
public class SemanticAnalysis {
	protected DataflowAnalysis dfa;
	private Database db;
	private MultiplexObserver<SemanticAnalysis> observable;

	public SemanticAnalysis(SyntaxAnalysis synA, Database db) {
		this.db = db;
		this.observable = new MultiplexObserver<SemanticAnalysis>();
		updateDFA(synA);
	}

	public void updateDFA(SyntaxAnalysis synA) {
		dfa = new DataflowAnalysis();
		HashMap<String, String> assignment = null;
		if (db.getSize() > 0)
			assignment = db.getRow(db.getCursor());
		dfa.init(assignment, db.getCursor(), synA.getSorted());
		observable.notify(this);

	}

	/**
	 * 
	 * @return DataFlowAnalysis of current assignment
	 */
	public DataflowAnalysis getDFA() {
		return dfa;
	}

	/**
	 * 
	 * @param synA
	 *            Checked SyntaxAnalysis
	 * @param i
	 *            Index of Database
	 * @return DataflowAnalysis of i-th assignment
	 */
	public DataflowAnalysis getDFA(SyntaxAnalysis synA, int i) {
		HashMap<String, String> assignment = null;
		if (i < db.getSize()) {
			assignment = db.getRow(i);
		}
		DataflowAnalysis dfa = new DataflowAnalysis();
		dfa.init(assignment, i, synA.getSorted());
		return dfa;
	}

	public Observable<SemanticAnalysis> getObservable() {
		return observable;
	}
}
