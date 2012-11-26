package org.vanda.studio.model;

import java.util.ArrayList;
import java.util.List;

import org.vanda.studio.model.hyper.AtomicJob;
import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.JobVisitor;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.hyper.MutableWorkflow.WorkflowChildEvent;
import org.vanda.studio.model.hyper.MutableWorkflow.WorkflowChildListener;
import org.vanda.studio.model.hyper.MutableWorkflow.WorkflowEvent;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.UnificationException;
import org.vanda.studio.util.TokenSource.Token;

public final class Model implements WorkflowChildListener {

	public static interface SelectionVisitor {
		void visitWorkflow(MutableWorkflow wf);

		void visitConnection(Token address,
				MutableWorkflow wf, Connection cc);

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
			v.visitConnection(address, workflow, workflow.getConnection(address));
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
	protected List<ImmutableWorkflow> unfolded;
	protected WorkflowSelection selection;
	protected List<SingleObjectSelection> markedElements;
	protected final MultiplexObserver<WorkflowChildEvent> childObservable;
	protected final MultiplexObserver<WorkflowEvent> workflowObservable;
	protected final MultiplexObserver<Model> selectionChangeObservable;
	protected final MultiplexObserver<Model> markedElementsObservable;
	protected final MultiplexObserver<Model> workflowCheckObservable;
	protected final JobVisitor bindVisitor;
	protected final JobVisitor unbindVisitor;

	public Model(MutableWorkflow hwf) {
		this.hwf = hwf;
		this.markedElements = new ArrayList<SingleObjectSelection>();
		childObservable = new MultiplexObserver<WorkflowChildEvent>();
		workflowObservable = new MultiplexObserver<WorkflowEvent>();
		selectionChangeObservable = new MultiplexObserver<Model>();
		markedElementsObservable = new MultiplexObserver<Model>();
		workflowCheckObservable = new MultiplexObserver<Model>();
		bindVisitor = new JobVisitor() {
			@Override
			public void visitAtomicJob(AtomicJob aj) {
			}

			@Override
			public void visitCompositeJob(CompositeJob cj) {
				bind(cj.getWorkflow());
			}

		};
		unbindVisitor = new JobVisitor() {
			@Override
			public void visitAtomicJob(AtomicJob aj) {
			}

			@Override
			public void visitCompositeJob(CompositeJob cj) {
				unbind(cj.getWorkflow());
			}

		};
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
		wf.visitAll(bindVisitor);
	}

	public void checkWorkflow() throws Exception {
		frozen = hwf.freeze();
		markedElements.clear();
		try {
			frozen.typeCheck();
		} catch (UnificationException e) {
			markedElements.add(new ConnectionSelection(hwf, e.getAddress()));
		}
		markedElementsObservable.notify(this);
		unfolded = frozen.unfold();
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

	public List<ImmutableWorkflow> getUnfolded() {
		return unfolded;
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
		wf.visitAll(unbindVisitor);
	}

	@Override
	public void childAdded(MutableWorkflow mwf, Job j) {
		j.visit(bindVisitor);
	}

	@Override
	public void childModified(MutableWorkflow mwf, Job j) {
	}

	@Override
	public void childRemoved(MutableWorkflow mwf, Job j) {
		j.visit(unbindVisitor);
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

	@Override
	public void inputPortAdded(MutableWorkflow mwf, Job j, int index) {
	}

	@Override
	public void inputPortRemoved(MutableWorkflow mwf, Job j, int index) {
	}

	@Override
	public void outputPortAdded(MutableWorkflow mwf, Job j, int index) {
	}

	@Override
	public void outputPortRemoved(MutableWorkflow mwf, Job j, int index) {
	}

}
