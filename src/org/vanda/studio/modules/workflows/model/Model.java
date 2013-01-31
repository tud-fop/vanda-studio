package org.vanda.studio.modules.workflows.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Pair;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.TypeCheckingException;
import org.vanda.workflows.hyper.Workflows.*;

public final class Model implements WorkflowListener<MutableWorkflow> {

	public static interface SelectionVisitor {
		void visitWorkflow(MutableWorkflow wf);

		void visitConnection(MutableWorkflow wf, ConnectionKey cc);

		void visitJob(MutableWorkflow wf, Job j);

		void visitVariable(Location variable, MutableWorkflow wf);
	}

	public static class WorkflowSelection {
		public final MutableWorkflow workflow;

		public WorkflowSelection(MutableWorkflow workflow) {
			this.workflow = workflow;
		}

		public void visit(SelectionVisitor v) {
			v.visitWorkflow(workflow);
		}
	}

	public abstract static class SingleObjectSelection extends
			WorkflowSelection {
		public SingleObjectSelection(MutableWorkflow workflow) {
			super(workflow);
		}

		@Override
		public abstract void visit(SelectionVisitor v);

		public abstract void remove();
	}

	public static class ConnectionSelection extends SingleObjectSelection {
		public final ConnectionKey cc;

		public ConnectionSelection(MutableWorkflow workflow, ConnectionKey cc) {
			super(workflow);
			this.cc = cc;
		}

		@Override
		public void remove() {
			workflow.removeConnection(cc);
		}

		@Override
		public void visit(SelectionVisitor v) {
			v.visitConnection(workflow, cc);
		}
	}

	public static class JobSelection extends SingleObjectSelection {
		public final Job job;

		public JobSelection(MutableWorkflow workflow, Job job) {
			super(workflow);
			this.job = job;
		}

		@Override
		public void remove() {
			workflow.removeChild(job);
		}

		@Override
		public void visit(SelectionVisitor v) {
			v.visitJob(workflow, job);
		}
	}

	public static class VariableSelection extends SingleObjectSelection {
		public final Location variable;
		
		public VariableSelection(MutableWorkflow workflow, Location variable) {
			super(workflow);
			this.variable = variable;
		}

		@Override
		public void remove() {
			// do nothing
		}

		@Override
		public void visit(SelectionVisitor v) {
			v.visitVariable(variable, workflow);
		}
	}

	protected final MutableWorkflow hwf;
	protected WorkflowSelection selection;
	protected List<SingleObjectSelection> markedElements;
	protected final MultiplexObserver<Model> selectionChangeObservable;
	protected final MultiplexObserver<Model> markedElementsObservable;
	protected final MultiplexObserver<Model> workflowCheckObservable;

	public Model(MutableWorkflow hwf) {
		this.hwf = hwf;
		markedElements = new ArrayList<SingleObjectSelection>();
		selectionChangeObservable = new MultiplexObserver<Model>();
		markedElementsObservable = new MultiplexObserver<Model>();
		workflowCheckObservable = new MultiplexObserver<Model>();
	}

	public void checkWorkflow() throws Exception {
		markedElements.clear();
		try {
			hwf.typeCheck();
		} catch (TypeCheckingException e) {
			List<Pair<String, Set<ConnectionKey>>> errors = e.getErrors();
			for (Pair<String, Set<ConnectionKey>> error : errors) {
				// TODO use new color in each iteration
				Set<ConnectionKey> eqs = error.snd;
				for (ConnectionKey eq : eqs) {
					markedElements.add(new ConnectionSelection(hwf, eq));
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
		markedElementsObservable.notify(this);
		workflowCheckObservable.notify(this);
	}

	public List<SingleObjectSelection> getMarkedElements() {
		return markedElements;
	}

	public MutableWorkflow getRoot() {
		return hwf;
	}

	public WorkflowSelection getSelection() {
		return selection;
	}

	public Observable<Model> getSelectionChangeObservable() {
		return selectionChangeObservable;
	}

	public Observable<Model> getMarkedElementsObservable() {
		return markedElementsObservable;
	}

	public Observable<Model> getWorkflowCheckObservable() {
		return workflowCheckObservable;
	}

	public void setSelection(WorkflowSelection selection) {
		this.selection = selection;
		selectionChangeObservable.notify(this);
	}

	public void setMarkedElements(List<SingleObjectSelection> elements) {
		markedElements = elements;
		markedElementsObservable.notify(this);
	}

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
	}
	

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childRemoved(MutableWorkflow mwf, Job j) {
		if (selection != null && selection.workflow == mwf)
			setSelection(null);
	}

	@Override
	public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
	}

	@Override
	public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
		if (selection != null && selection.workflow == mwf)
			setSelection(null);
	}

	@Override
	public void propertyChanged(MutableWorkflow mwf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updated(MutableWorkflow mwf) {
		// TODO Auto-generated method stub
		
	}

}
