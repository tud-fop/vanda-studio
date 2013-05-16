package org.vanda.workflows.elements;

public class Elements {

	public static interface ElementListener<E> {
		// removed: see older versions
		// void inputPortAdded(Element e, int index);
		// void inputPortRemoved(Element e, int index);
		void typeChanged(E e);
		void valueChanged(E e);
	}
	
	public static interface ElementEvent<E> {
		void doNotify(ElementListener<E> el);
	}
	
	public static class TypeChangeEvent<E> implements ElementEvent<E> {
		
		private final E e;
		
		public TypeChangeEvent(E e) {
			this.e = e;
		}

		@Override
		public void doNotify(ElementListener<E> el) {
			el.typeChanged(e);
		}
		
	}

	public static class ValueChangeEvent<E> implements ElementEvent<E> {
		
		private final E e;
		
		public ValueChangeEvent(E e) {
			this.e = e;
		}

		@Override
		public void doNotify(ElementListener<E> el) {
			el.valueChanged(e);
		}
		
	}

}
