package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.util.TokenSource.Token;

public interface ElementEditorFactory<T> {
	JComponent createEditor(Application app, MutableWorkflow wf, Token address,
			T o);
}
