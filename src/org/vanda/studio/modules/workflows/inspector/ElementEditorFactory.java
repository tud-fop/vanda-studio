package org.vanda.studio.modules.workflows.inspector;

import java.util.List;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.util.TokenSource.Token;

public interface ElementEditorFactory<T> {
	JComponent createEditor(Application app, List<Token> path, Token address,
			T o);
}
