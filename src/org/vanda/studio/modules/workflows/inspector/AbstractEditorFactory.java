package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.workflows.data.Database;

public interface AbstractEditorFactory {

	JComponent createEditor(Database d);
	
}
