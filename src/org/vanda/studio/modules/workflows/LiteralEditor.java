package org.vanda.studio.modules.workflows;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.vanda.studio.model.elements.Literal;

public class LiteralEditor implements ElementEditorFactory {

	@Override
	public JComponent createEditor(Object o) {
		if (o instanceof Literal) {
			JPanel editor = new JPanel();
			return editor;
		}
		else
			return null;
	}

}
