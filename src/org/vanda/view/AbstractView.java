package org.vanda.view;

import org.vanda.execution.model.Runables.RunState;
import org.vanda.util.MultiplexObserver;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

/**
 * stores selection, highlighting and run information
 * 
 * @author kgebhardt
 * 
 */
public abstract class AbstractView {
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

	public static class RunProgressUpdateEvent<V> implements ViewEvent<V> {
		private final V v;

		public RunProgressUpdateEvent(V v) {
			this.v = v;
		}

		@Override
		public void doNotify(ViewListener<V> vl) {
			vl.runProgressUpdate(v);
		}
	}

	public static class RunStateTransitionEvent<V> implements ViewEvent<V> {
		private final V v;
		private final RunState from, to;

		public RunStateTransitionEvent(V v, RunState from, RunState to) {
			this.v = v;
			this.from = from;
			this.to = to;
		}

		@Override
		public void doNotify(ViewListener<V> vl) {
			vl.runStateTransition(v, from, to);
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

		void visitVariable(Location variable, MutableWorkflow wf);

		void visitWorkflow(MutableWorkflow wf);
	}

	public static interface ViewEvent<V> {
		void doNotify(ViewListener<V> vl);

	}

	public static interface ViewListener<V> {
		void highlightingChanged(V v);

		void markChanged(V v);

		void selectionChanged(V v);

		void runProgressUpdate(V v);

		void runStateTransition(V v, RunState from, RunState to);
	}

	boolean highlighted;

	boolean marked;

	private MultiplexObserver<ViewEvent<AbstractView>> observable = new MultiplexObserver<ViewEvent<AbstractView>>();

	boolean selected;

	public MultiplexObserver<ViewEvent<AbstractView>> getObservable() {
		return observable;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public boolean isMarked() {
		return marked;
	}

	public boolean isSelected() {
		return selected;
	}

	public abstract void remove(View view);

	/**
	 * not used: highlighted == marked
	 * TODO: clean up!
	 * @param highlighted
	 */
	public void setHighlighted(boolean highlighted) {
		if (this.highlighted != highlighted) {
			this.highlighted = highlighted;
			// observable.notify(new HighEvent<AbstractView>(this));
		}

	}

	public void setMarked(boolean marked) {
		if (this.marked != marked) {
			this.marked = marked;
			observable.notify(new MarkChangedEvent<AbstractView>(this));
		}

	}

	public void setSelected(boolean selected) {
		if (this.selected != selected) {
			this.selected = selected;
			observable.notify(new SelectionChangedEvent<AbstractView>(this));
		}
	}

	public abstract void visit(SelectionVisitor sv, View view);
}