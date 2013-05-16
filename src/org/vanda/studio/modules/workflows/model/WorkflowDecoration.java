package org.vanda.studio.modules.workflows.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Pair;
import org.vanda.view.View;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.TypeChecker;
import org.vanda.workflows.hyper.TypeCheckingException;
import org.vanda.workflows.hyper.Workflows.*;

public final class WorkflowDecoration implements WorkflowListener<MutableWorkflow> {

	protected final View view;
	protected Job[] sorted = null;
	private Map<Object, Type> types = Collections.emptyMap();
	private Type fragmentType = null;
	protected final MultiplexObserver<WorkflowDecoration> workflowCheckObservable;

	public WorkflowDecoration(View view) {
		this.view = view;
		workflowCheckObservable = new MultiplexObserver<WorkflowDecoration>();
	}
	
	public void typeCheck() throws TypeCheckingException {
		TypeChecker tc = new TypeChecker();
		view.getWorkflow().typeCheck(tc);
		tc.check();
		types = tc.getTypes();
		fragmentType = tc.getFragmentType();
		
	}

	public void checkWorkflow() throws Exception {
		view.clearMarked();
		try {
			sorted = null;
			typeCheck();
			sorted = view.getWorkflow().getSorted();
		} catch (TypeCheckingException e) {
			List<Pair<String, Set<ConnectionKey>>> errors = e.getErrors();
			for (Pair<String, Set<ConnectionKey>> error : errors) {
				// TODO use new color in each iteration
				Set<ConnectionKey> eqs = error.snd;
				for (ConnectionKey eq : eqs) {
					view.getConnectionView(eq).setMarked(true);
					/*
					for (Connection c : hwf.getConnections()) {
						if (c.target.equals(eq.address)
								&& c.targetPort == eq.port) {
							markedElements.add(new ConnectionSelection(hwf,
									c.address));
							break;
						}
					}
					*/
				}
			}

		}
		//markedElementsObservable.notify(this);
		workflowCheckObservable.notify(this);
	}

	public MutableWorkflow getRoot() {
		return view.getWorkflow();
	}
	
	public Job[] getSorted() {
		return sorted;
	}

	public Observable<WorkflowDecoration> getWorkflowCheckObservable() {
		return workflowCheckObservable;
	}

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
	}
	

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childRemoved(MutableWorkflow mwf, Job j) {
//		if (selection != null && selection.workflow == mwf)
//			setSelection(null);
	}

	@Override
	public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
//		if (selection != null && selection.workflow == mwf)
//			setSelection(null);
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updated(MutableWorkflow mwf) {
		// TODO Auto-generated method stub
		
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	public Type getType(Object variable) {
		return types.get(variable);
	}

	public View getView() {
		return this.view;
	}
}
