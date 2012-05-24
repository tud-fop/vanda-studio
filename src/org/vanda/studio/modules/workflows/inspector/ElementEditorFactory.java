package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;

public interface ElementEditorFactory<T> {
	JComponent createEditor(Application app, T o);
}
