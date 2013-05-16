package org.vanda.datasources;

import javax.swing.JComponent;

public interface ElementSelector {

	JComponent getComponent();
	Element getElement();
	void setElement(Element e);
	
}
