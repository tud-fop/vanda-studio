package org.vanda.studio.modules.profile.model;

import org.vanda.studio.modules.profile.fragments.DataflowAnalysis;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;

public class Model {
	
	// private final org.vanda.studio.model.Model model;
	private DataflowAnalysis dfa;
	private MultiplexObserver<DataflowAnalysis> dfaChangedObservable;
	
	public Model(org.vanda.studio.model.Model model) {
		// this.model = model;
		dfaChangedObservable = new MultiplexObserver<DataflowAnalysis>();
		model.getWorkflowCheckObservable().addObserver(new Observer<org.vanda.studio.model.Model>() {

			@Override
			public void notify(org.vanda.studio.model.Model event) {
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
