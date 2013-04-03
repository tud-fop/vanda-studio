package org.vanda.studio.modules.workflows.model;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.vanda.studio.app.Application;
import org.vanda.util.Action;

public interface WorkflowEditor {
	void addAction(Action a, KeyStroke keyStroke);
	void addToolWindow(JComponent c);
	void focusToolWindow(JComponent c);
	Application getApplication();
	WorkflowDecoration getWorkflowDecoration();
	void removeToolWindow(JComponent c);
	void setPalette(JComponent c);
}
