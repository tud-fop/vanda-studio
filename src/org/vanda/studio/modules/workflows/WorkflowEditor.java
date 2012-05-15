package org.vanda.studio.modules.workflows;

import javax.swing.JComponent;

public interface WorkflowEditor {
	void addToolWindow(JComponent c);
	void focusToolWindow(JComponent c);
	void removeToolWindow(JComponent c);
}
