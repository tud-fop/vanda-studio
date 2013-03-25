package org.vanda.view;

import org.vanda.util.MultiplexObserver;
/**
 * stores selection / highlighting information.
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
		if (this.selected != selected)
		{
			this.selected = selected;
			observable.notify(new SelectionChangedEvent<AbstractView>(this));
		}
	}
	
	public boolean isHighlighted() {
		return highlighted;
	}
	public void setHighlighted(boolean highlighted) {
		if (this.highlighted != highlighted) 
		{
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
}