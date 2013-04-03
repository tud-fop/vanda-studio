package org.vanda.fragment.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.TypeChecker;
import org.vanda.workflows.hyper.TypeCheckingException;
import org.vanda.workflows.hyper.Workflows.WorkflowEvent;
import org.vanda.workflows.hyper.Workflows.WorkflowListener;

public class Model implements Observer<WorkflowEvent<MutableWorkflow>>, WorkflowListener<MutableWorkflow> {
	
	// private final org.vanda.studio.model.Model model;
	private MutableWorkflow hwf;
	protected Job[] sorted = null;
	private Map<Object, Type> types = Collections.emptyMap();
	private Type fragmentType = null;
	private DataflowAnalysis dfa;
	private MultiplexObserver<DataflowAnalysis> dfaChangedObservable;
	
	public Model(org.vanda.studio.modules.workflows.model.WorkflowDecoration model) {
		hwf = model.getRoot();
		hwf.getObservable().addObserver(this);
		// this.model = model;
		dfaChangedObservable = new MultiplexObserver<DataflowAnalysis>();
	}
	
	public void typeCheck() throws TypeCheckingException {
		TypeChecker tc = new TypeChecker();
		hwf.typeCheck(tc);
		tc.check();
		types = tc.getTypes();
		fragmentType = tc.getFragmentType();
		
	}

	public void checkWorkflow() {
		// markedElements.clear();
		try {
			sorted = null;
			typeCheck();
			try {
				sorted = hwf.getSorted();
			} catch (Exception e) {
				// FIXME send message that there are cycles
			}
			dfa = new DataflowAnalysis(hwf, sorted, fragmentType);
			dfa.init();
			dfaChangedObservable.notify(dfa);
		} catch (TypeCheckingException e) {
			List<Pair<String, Set<ConnectionKey>>> errors = e.getErrors();
			for (Pair<String, Set<ConnectionKey>> error : errors) {
				// TODO use new color in each iteration
				Set<ConnectionKey> eqs = error.snd;
				// for (ConnectionKey eq : eqs)
				// 	markedElements.add(new ConnectionSelection(hwf, eq));
			}

		}
		// markedElementsObservable.notify(this);
		// weg workflowCheckObservable.notify(this);
	}
	
	public Job[] getSorted() {
		return sorted;
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	public Type getType(Object variable) {
		return types.get(variable);
	}

	public DataflowAnalysis getDataflowAnalysis() {
		return dfa;
	}
	
	public Observable<DataflowAnalysis> getDfaChangedObservable() {
		return dfaChangedObservable;
	}

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childRemoved(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
	}

	@Override
	public void updated(MutableWorkflow mwf) {
		checkWorkflow();
	}

	@Override
	public void notify(WorkflowEvent<MutableWorkflow> event) {
		event.doNotify(this);
	}

}
