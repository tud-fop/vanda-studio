package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;

public interface ElementEditorFactory<T> {
	JComponent createEditor(Database d, MutableWorkflow wf, T o);
}
