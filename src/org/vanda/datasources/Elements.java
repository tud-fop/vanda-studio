package org.vanda.datasources;

public class Elements {

	public static interface ElementListener<E> {
		void prefixChanged(E e);
		void valueChanged(E e);
	}
	
	public static interface ElementEvent<E> {
		void doNotify(ElementListener<E> el);
		E getElement();
	}
	
	public static class PrefixChangeEvent<E> implements ElementEvent<E> {
		
		private final E e;
		
		public PrefixChangeEvent(E e) {
			this.e = e;
		}

		@Override
		public void doNotify(ElementListener<E> el) {
			el.prefixChanged(e);
		}

		@Override
		public E getElement() {
			return e;
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

		@Override
		public E getElement() {
			return e;
		}
		
	}

}
