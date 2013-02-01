package org.vanda.workflows.elements;

public class Elements {

	public static interface ElementListener<E> {
		// removed: see older versions
		// void inputPortAdded(Element e, int index);
		// void inputPortRemoved(Element e, int index);
		void propertyChanged(E e);
	}
	
	public static interface ElementEvent<E> {
		void doNotify(ElementListener<E> el);
	}
	
	public static class PropertyChangeEvent<E> implements ElementEvent<E> {
		
		private final E e;
		
		public PropertyChangeEvent(E e) {
			this.e = e;
		}

		@Override
		public void doNotify(ElementListener<E> el) {
			el.propertyChanged(e);
		}
		
	}

}
