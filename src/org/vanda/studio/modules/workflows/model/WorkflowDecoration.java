package org.vanda.studio.modules.workflows.model;

import java.util.ArrayList;
import java.util.List;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Workflows.*;

public final class WorkflowDecoration  {

//	public static interface SelectionVisitor {
//		void visitWorkflow(MutableWorkflow wf);
//
//		void visitConnection(MutableWorkflow wf, ConnectionKey cc);
//
//		void visitJob(MutableWorkflow wf, Job j);
//
//		void visitVariable(Location variable, MutableWorkflow wf);
//	}
//
//	public static class WorkflowSelection {
//		public final MutableWorkflow workflow;
//
//		public WorkflowSelection(MutableWorkflow workflow) {
//			this.workflow = workflow;
//		}
//
//		public void visit(SelectionVisitor v) {
//			v.visitWorkflow(workflow);
//		}
//	}
//
//	public abstract static class SingleObjectSelection extends
//			WorkflowSelection {
//		public SingleObjectSelection(MutableWorkflow workflow) {
//			super(workflow);
//		}
//
//		@Override
//		public abstract void visit(SelectionVisitor v);
//
//		public abstract void remove();
//	}
//
//	public static class ConnectionSelection extends SingleObjectSelection {
//		public final ConnectionKey cc;
//
//		public ConnectionSelection(MutableWorkflow workflow, ConnectionKey cc) {
//			super(workflow);
//			this.cc = cc;
//		}
//
//		@Override
//		public void remove() {
//			workflow.removeConnection(cc);
//		}
//
//		@Override
//		public void visit(SelectionVisitor v) {
//			v.visitConnection(workflow, cc);
//		}
//	}
//
//	public static class JobSelection extends SingleObjectSelection {
//		public final Job job;
//
//		public JobSelection(MutableWorkflow workflow, Job job) {
//			super(workflow);
//			this.job = job;
//		}
//
//		@Override
//		public void remove() {
//			workflow.removeChild(job);
//		}
//
//		@Override
//		public void visit(SelectionVisitor v) {
//			v.visitJob(workflow, job);
//		}
//	}
//
//	public static class VariableSelection extends SingleObjectSelection {
//		public final Location variable;
//		
//		public VariableSelection(MutableWorkflow workflow, Location variable) {
//			super(workflow);
//			this.variable = variable;
//		}
//
//		@Override
//		public void remove() {
//			// do nothing
//		}
//
//		@Override
//		public void visit(SelectionVisitor v) {
//			v.visitVariable(variable, workflow);
//		}
//	}

	protected final MutableWorkflow hwf;
//	protected WorkflowSelection selection;
//	protected List<SingleObjectSelection> markedElements;
	protected final MultiplexObserver<WorkflowDecoration> selectionChangeObservable;
	protected final MultiplexObserver<WorkflowDecoration> markedElementsObservable;
	protected final MultiplexObserver<WorkflowDecoration> workflowCheckObservable;

	public WorkflowDecoration(MutableWorkflow hwf) {
		this.hwf = hwf;
//		markedElements = new ArrayList<SingleObjectSelection>();
		selectionChangeObservable = new MultiplexObserver<WorkflowDecoration>();
		markedElementsObservable = new MultiplexObserver<WorkflowDecoration>();
		workflowCheckObservable = new MultiplexObserver<WorkflowDecoration>();
	}

//	public List<SingleObjectSelection> getMarkedElements() {
//		return markedElements;
//	}

	public MutableWorkflow getRoot() {
		return hwf;
	}

//	public WorkflowSelection getSelection() {
//		return selection;
//	}

	public Observable<WorkflowDecoration> getSelectionChangeObservable() {
		return selectionChangeObservable;
	}

	public Observable<WorkflowDecoration> getMarkedElementsObservable() {
		return markedElementsObservable;
	}

	public Observable<WorkflowDecoration> getWorkflowCheckObservable() {
		return workflowCheckObservable;
	}

//	public void setSelection(WorkflowSelection selection) {
//		this.selection = selection;
//		selectionChangeObservable.notify(this);
//	}

//	public void setMarkedElements(List<SingleObjectSelection> elements) {
//		markedElements = elements;
//		markedElementsObservable.notify(this);
//	}

//	@Override
//	public void childAdded(MutableWorkflow mwf, Job j) {
//	}
//	
//
//	@Override
//	public void childModified(MutableWorkflow mwf, Job j) {
//	}

//	@Override
//	public void childRemoved(MutableWorkflow mwf, Job j) {
//		if (selection != null && selection.workflow == mwf)
//			setSelection(null);
//	}

//	@Override
//	public void connectionAdded(MutableWorkflow mwf, ConnectionKey cc) {
//	}

//	@Override
//	public void connectionRemoved(MutableWorkflow mwf, ConnectionKey cc) {
//		if (selection != null && selection.workflow == mwf)
//			setSelection(null);
//	}

//	@Override
//	public void propertyChanged(MutableWorkflow mwf) {
//	}
//
//	@Override
//	public void updated(MutableWorkflow mwf) {
//	}

}
