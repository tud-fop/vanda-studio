package org.vanda.studio.modules.workflows;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.util.Action;

public interface WorkflowEditor {
	void addAction(Action a);
	void addToolWindow(JComponent c);
	void focusToolWindow(JComponent c);
	Application getApplication();
	void removeToolWindow(JComponent c);
}
