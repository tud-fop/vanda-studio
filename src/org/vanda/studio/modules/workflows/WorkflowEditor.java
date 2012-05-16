package org.vanda.studio.modules.workflows;

import javax.swing.JComponent;

import org.vanda.studio.util.Action;

public interface WorkflowEditor {
	void addAction(Action a);
	void addToolWindow(JComponent c);
	void focusToolWindow(JComponent c);
	void removeToolWindow(JComponent c);
}
