package org.vanda.studio.modules.workflows.inspector;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.vanda.studio.model.hyper.MutableWorkflow;

public class WorkflowEditor implements ElementEditorFactory {

	@Override
	public JComponent createEditor(Object o) {
		if (o instanceof MutableWorkflow) {
			final MutableWorkflow mwf = (MutableWorkflow) o;
			final JLabel label = new JLabel("Name:");
			final JTextField text = new JTextField(mwf.getName(), 20);
			text.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void insertUpdate(DocumentEvent e) {
					mwf.setName(text.getText());
				}

				@Override
				public void removeUpdate(DocumentEvent e) {
					mwf.setName(text.getText());
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) {
				}
				
			});

			JPanel editor = new JPanel();
			GroupLayout layout = new GroupLayout(editor);
			editor.setLayout(layout);
			layout.setAutoCreateGaps(true);
			layout.setAutoCreateContainerGaps(true);
			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(label).addComponent(text));
			layout.setVerticalGroup(layout
					.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(label).addComponent(text));
			return editor;
		} else
			return null;
	}

}
