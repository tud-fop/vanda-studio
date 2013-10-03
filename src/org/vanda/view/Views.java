package org.vanda.view;

import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

public final class Views {

	public static class HighlightingChangedEvent<V> implements ViewEvent<V> {
		private final V v;

		public HighlightingChangedEvent(V v) {
			this.v = v;
		}

		@Override
		public void doNotify(ViewListener<V> vl) {
			vl.highlightingChanged(v);
		}

	}

	public static class MarkChangedEvent<V> implements ViewEvent<V> {
		private final V v;

		public MarkChangedEvent(V v) {
			this.v = v;
		}

		@Override
		public void doNotify(ViewListener<V> vl) {
			vl.markChanged(v);
		}

	}

	public static class SelectionChangedEvent<V> implements ViewEvent<V> {
		private final V v;

		public SelectionChangedEvent(V v) {
			this.v = v;
		}

		@Override
		public void doNotify(ViewListener<V> vl) {
			vl.selectionChanged(v);
		}
	}

	public static interface SelectionVisitor {
		void visitConnection(MutableWorkflow wf, ConnectionKey cc);

		void visitJob(MutableWorkflow wf, Job j);

		void visitVariable(MutableWorkflow wf, Location variable);

		void visitWorkflow(MutableWorkflow wf);
	}

	public static interface SelectionObject {
		void visit(SelectionVisitor sv, MutableWorkflow wf);
		
		void remove(MutableWorkflow wf);
	}
	
	public static class ConnectionSelectionObject implements SelectionObject {
		private final ConnectionKey cc;
		
		public ConnectionSelectionObject(ConnectionKey cc) {
			this.cc = cc;
		}

		@Override
		public void visit(SelectionVisitor sv, MutableWorkflow wf) {
			sv.visitConnection(wf, cc);
		}

		@Override
		public void remove(MutableWorkflow wf) {
			wf.removeConnection(cc);
		}
	}
	
	public static class JobSelectionObject implements SelectionObject {
		private final Job j;
		
		public JobSelectionObject(Job j) {
			this.j = j;
		}

		@Override
		public void visit(SelectionVisitor sv, MutableWorkflow wf) {
			sv.visitJob(wf, j);
		}

		@Override
		public void remove(MutableWorkflow wf) {
			wf.removeChild(j);
		}
	}
	
	public static class LocationSelectionObject implements SelectionObject {
		private final Location l;
		
		public LocationSelectionObject(Location l) {
			this.l = l;
		}

		@Override
		public void visit(SelectionVisitor sv, MutableWorkflow wf) {
			sv.visitVariable(wf, l);
		}

		@Override
		public void remove(MutableWorkflow wf) {
			// do nothing
		}
	}
	
	public static class WorkflowSelectionObject implements SelectionObject {
		private final MutableWorkflow wf;
		
		public WorkflowSelectionObject(MutableWorkflow wf) {
			this.wf = wf;
		}

		@Override
		public void visit(SelectionVisitor sv, MutableWorkflow wf) {
			sv.visitWorkflow(this.wf);
		}

		@Override
		public void remove(MutableWorkflow wf) {
			// do nothing
		}
	}

	public static interface ViewEvent<V> {
		void doNotify(ViewListener<V> vl);

	}

	public static interface ViewListener<V> {
		void highlightingChanged(V v);

		void markChanged(V v);

		void selectionChanged(V v);
	}

}
