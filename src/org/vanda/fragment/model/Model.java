package org.vanda.fragment.model;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;

public class Model {
	
	// private final org.vanda.studio.model.Model model;
	private DataflowAnalysis dfa;
	private MultiplexObserver<DataflowAnalysis> dfaChangedObservable;
	
	public Model(org.vanda.studio.modules.workflows.model.WorkflowDecoration model) {
		// this.model = model;
		dfaChangedObservable = new MultiplexObserver<DataflowAnalysis>();
		model.getWorkflowCheckObservable().addObserver(new Observer<org.vanda.studio.modules.workflows.model.WorkflowDecoration>() {

			@Override
			public void notify(org.vanda.studio.modules.workflows.model.WorkflowDecoration event) {
				dfa = new DataflowAnalysis(event.getRoot(), event.getSorted(), event.getFragmentType());
				dfa.init();
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
