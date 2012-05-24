package org.vanda.studio.model.elements;

import org.vanda.studio.model.elements.Element.ElementListener;

public class Elements {

	public static class InputPortAddEvent implements Element.ElementEvent {
		
		private final Element e;
		private final int index;
		
		public InputPortAddEvent(Element e, int index) {
			this.e = e;
			this.index = index;
		}
	
		@Override
		public void doNotify(Element.ElementListener el) {
			el.inputPortAdded(e, index);
		}
	
	}

	public static class InputPortRemoveEvent implements Element.ElementEvent {
		
		private final Element e;
		private final int index;
		
		public InputPortRemoveEvent(Element e, int index) {
			this.e = e;
			this.index = index;
		}
	
		@Override
		public void doNotify(Element.ElementListener el) {
			el.inputPortRemoved(e, index);
		}
	
	}
	
	public static class PropertyChangeEvent implements Element.ElementEvent {
		
		private final Element e;
		
		public PropertyChangeEvent(Element e) {
			this.e = e;
		}

		@Override
		public void doNotify(ElementListener el) {
			el.propertyChanged(e);
		}
		
		
		
	}

}
