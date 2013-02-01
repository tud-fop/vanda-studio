package org.vanda.workflows.hyper;


public final class ElementAdapters {

	public static interface ElementAdapterListener<E> {
		// removed: see older versions
		// void inputPortAdded(Element e, int index);
		// void inputPortRemoved(Element e, int index);
		void propertyChanged(E e);
	}
	
	public static interface ElementAdapterEvent<E> {
		void doNotify(ElementAdapterListener<E> el);
	}
	
	public static class PropertyChangedEvent<E> implements ElementAdapterEvent<E> {
		
		private final E j;
		
		public PropertyChangedEvent(E j) {
			this.j = j;
		}

		@Override
		public void doNotify(ElementAdapterListener<E> jl) {
			jl.propertyChanged(j);
		}

	}

}
