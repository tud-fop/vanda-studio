package org.vanda.workflows.elements;

import org.vanda.workflows.elements.Element.ElementListener;

public class Elements {
	
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
