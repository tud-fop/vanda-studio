package org.vanda.studio.modules.workflows.inspector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.types.Type;

public class LiteralEditor implements ElementEditorFactory<Literal> {

	@Override
	public JComponent createEditor(Application app, final Literal l) {
		final JLabel label1 = new JLabel("Type:");
		final JLabel label2 = new JLabel("Value:");
		final JComboBox typeBox = new JComboBox();
		for (Type t : app.getTypes())
			typeBox.addItem(t);
		typeBox.setEditable(true);
		typeBox.setSelectedItem(l.getType());
		typeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				l.setType((Type) typeBox.getSelectedItem());
			}
		});
		
		final JComboBox valueBox = new JComboBox();
		valueBox.setEditable(true);
		valueBox.setSelectedItem(l.getValue());
		valueBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				l.setValue(valueBox.getSelectedItem().toString());
			}
		});

		JPanel editor = new JPanel();
		GroupLayout layout = new GroupLayout(editor);
		editor.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup().addComponent(label1)
								.addComponent(label2))
				.addGroup(
						layout.createParallelGroup().addComponent(typeBox)
								.addComponent(valueBox)));
		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(label1).addComponent(typeBox))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(label2)
								.addComponent(valueBox)));
		return editor;
	}

}
