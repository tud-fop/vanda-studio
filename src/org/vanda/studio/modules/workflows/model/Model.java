package org.vanda.studio.modules.workflows.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.hyper.Connection;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.MutableWorkflow.WorkflowChildEvent;
import org.vanda.workflows.hyper.MutableWorkflow.WorkflowChildListener;
import org.vanda.workflows.hyper.MutableWorkflow.WorkflowEvent;
import org.vanda.workflows.immutable.ImmutableWorkflow;
import org.vanda.workflows.immutable.TypeCheckingException;
import org.vanda.workflows.immutable.TypeChecker.EqInfo;

public final class Model implements WorkflowChildListener {

	public static interface SelectionVisitor {
		void visitWorkflow(MutableWorkflow wf);

		void visitConnection(Token address, MutableWorkflow wf, Connection cc);

		void visitJob(Token address, MutableWorkflow wf, Job j);

		void visitVariable(Token variable, MutableWorkflow wf);
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
		public final Token address;

		public SingleObjectSelection(MutableWorkflow workflow, Token address) {
			super(workflow);
			this.address = address;
		}

		@Override
		public abstract void visit(SelectionVisitor v);

		public abstract void remove();
	}

	public static class ConnectionSelection extends SingleObjectSelection {

		public ConnectionSelection(MutableWorkflow workflow, Token address) {
			super(workflow, address);
		}

		@Override
		public void remove() {
			workflow.removeConnection(address);
		}

		@Override
		public void visit(SelectionVisitor v) {
			v.visitConnection(address, workflow,
					workflow.getConnection(address));
		}
	}

	public static class JobSelection extends SingleObjectSelection {
		public JobSelection(MutableWorkflow workflow, Token address) {
			super(workflow, address);
		}

		@Override
		public void remove() {
			workflow.removeChild(address);
		}

		@Override
		public void visit(SelectionVisitor v) {
			v.visitJob(address, workflow, workflow.getChild(address));
		}
	}

	public static class VariableSelection extends SingleObjectSelection {
		public VariableSelection(MutableWorkflow workflow, Token variable) {
			super(workflow, variable);
		}

		@Override
		public void remove() {
			// do nothing
		}

		@Override
		public void visit(SelectionVisitor v) {
			v.visitVariable(address, workflow);
		}
	}

	protected final MutableWorkflow hwf;
	protected ImmutableWorkflow frozen;
	protected WorkflowSelection selection;
	protected List<SingleObjectSelection> markedElements;
	protected final MultiplexObserver<WorkflowChildEvent> childObservable;
	protected final MultiplexObserver<WorkflowEvent> workflowObservable;
	protected final MultiplexObserver<Model> selectionChangeObservable;
	protected final MultiplexObserver<Model> markedElementsObservable;
	protected final MultiplexObserver<Model> workflowCheckObservable;

	public Model(MutableWorkflow hwf) {
		this.hwf = hwf;
		this.markedElements = new ArrayList<SingleObjectSelection>();
		childObservable = new MultiplexObserver<WorkflowChildEvent>();
		workflowObservable = new MultiplexObserver<WorkflowEvent>();
		selectionChangeObservable = new MultiplexObserver<Model>();
		markedElementsObservable = new MultiplexObserver<Model>();
		workflowCheckObservable = new MultiplexObserver<Model>();
		childObservable.addObserver(new Observer<WorkflowChildEvent>() {
			@Override
			public void notify(WorkflowChildEvent event) {
				event.doNotify(Model.this);
			}
		});
		bind(hwf);
	}

	private void bind(MutableWorkflow wf) {
		wf.getObservable().addObserver(workflowObservable);
		wf.getChildObservable().addObserver(childObservable);
	}

	public void checkWorkflow() throws Exception {
		frozen = hwf.freeze();
		markedElements.clear();
		try {
			frozen.typeCheck();
		} catch (TypeCheckingException e) {
			List<Pair<String, Set<EqInfo>>> errors = e.getErrors();
			for (Pair<String, Set<EqInfo>> error : errors) {
				// TODO use new color in each iteration
				Set<EqInfo> eqs = error.snd;
				for (EqInfo eq : eqs) {
					for (Connection c : hwf.getConnections()) {
						if (c.target.equals(eq.address)
								&& c.targetPort == eq.port) {
							markedElements.add(new ConnectionSelection(hwf,
									c.address));
							break;
						}
					}

				}
			}

		}
		markedElementsObservable.notify(this);
		workflowCheckObservable.notify(this);
	}

	public Observable<WorkflowChildEvent> getChildObservable() {
		return childObservable;
	}

	public Observable<WorkflowEvent> getWorkflowObservable() {
		return workflowObservable;
	}

	public ImmutableWorkflow getFrozen() {
		return frozen;
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

	public void unbind(MutableWorkflow wf) {
		wf.getObservable().removeObserver(workflowObservable);
		wf.getChildObservable().removeObserver(childObservable);
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
	public void connectionAdded(MutableWorkflow mwf, Connection cc) {
	}

	@Override
	public void connectionRemoved(MutableWorkflow mwf, Connection cc) {
		if (selection != null && selection.workflow == mwf)
			setSelection(null);
	}

}
