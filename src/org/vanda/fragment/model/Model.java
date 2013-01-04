package org.vanda.fragment.model;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;

public class Model {
	
	// private final org.vanda.studio.model.Model model;
	private DataflowAnalysis dfa;
	private MultiplexObserver<DataflowAnalysis> dfaChangedObservable;
	
	public Model(org.vanda.studio.modules.workflows.model.Model model) {
		// this.model = model;
		dfaChangedObservable = new MultiplexObserver<DataflowAnalysis>();
		model.getWorkflowCheckObservable().addObserver(new Observer<org.vanda.studio.modules.workflows.model.Model>() {

			@Override
			public void notify(org.vanda.studio.modules.workflows.model.Model event) {
				dfa = new DataflowAnalysis(event.getFrozen());
				dfaChangedObservable.notify(dfa);
			}
			
		});
	}
	
	public DataflowAnalysis getDataflowAnalysis() {
		return dfa;
	}
	
	public Observable<DataflowAnalysis> getDfaChangedObservable() {
		return dfaChangedObservable;
	}

}
