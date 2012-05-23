package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

public interface ElementEditorFactory<T> {
	JComponent createEditor(T o);
}
