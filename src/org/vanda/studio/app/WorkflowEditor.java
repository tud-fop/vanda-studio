package org.vanda.studio.app;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.vanda.studio.util.Action;

public interface WorkflowEditor {
	void addAction(Action a, KeyStroke keyStroke);
	void addToolWindow(JComponent c);
	void focusToolWindow(JComponent c);
	Application getApplication();
	void removeToolWindow(JComponent c);
}