package org.vanda.view;

import org.vanda.util.MultiplexObserver;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

/**
 * stores selection / highlighting information.
 * 
 * @author kgebhardt
 * 
 */
public abstract class AbstractView {
	boolean selected;
	boolean highlighted;
	private MultiplexObserver<ViewEvent<AbstractView>> observable;

	public MultiplexObserver<ViewEvent<AbstractView>> getObservable() {
		return observable;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		if (this.selected != selected) {
			this.selected = selected;
			observable.notify(new SelectionChangedEvent<AbstractView>(this));
		}
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setHighlighted(boolean highlighted) {
		if (this.highlighted != highlighted) {
			this.highlighted = highlighted;
			observable.notify(new SelectionChangedEvent<AbstractView>(this));
		}

	}

	public static interface ViewEvent<V> {
		void doNotify(ViewListener<V> vl);

	}

	public static interface ViewListener<V> {
		void selectionChanged(V v);
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

	public abstract void remove(View view);
	public abstract void visit(SelectionVisitor sv, View view);
	
	public static interface SelectionVisitor {
		void visitWorkflow(MutableWorkflow wf);

		void visitConnection(MutableWorkflow wf, ConnectionKey cc);

		void visitJob(MutableWorkflow wf, Job j);

		void visitVariable(Location variable, MutableWorkflow wf);
}
}