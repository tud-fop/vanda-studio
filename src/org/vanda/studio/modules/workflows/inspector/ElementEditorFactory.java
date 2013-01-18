package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.workflows.hyper.MutableWorkflow;

public interface ElementEditorFactory<T> {
	JComponent createEditor(Application app, MutableWorkflow wf, T o);
}
