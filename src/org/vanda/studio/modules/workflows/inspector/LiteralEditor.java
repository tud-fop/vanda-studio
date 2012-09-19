package org.vanda.studio.modules.workflows.inspector;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

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
				Type t = (Type) typeBox.getSelectedItem();
				l.setType(t);
			}
		});
		
		final JTextField value = new JTextField(l.getValue());
		value.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				l.setValue(value.getText());
				
			}
		});

		final Action aValue = new AbstractAction("...") {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String prefix = System.getProperty("user.home") + "/.vanda/input/";
				File f = new File(prefix);
				FileSystemView rfsv = new RestrictedFileSystemView(f);
				JFileChooser fc = new JFileChooser(rfsv);
				int returnVal = fc.showOpenDialog(null);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            String choice = fc.getSelectedFile().getAbsolutePath().replaceFirst(prefix, "");
		        	value.setText(choice);
		            l.setValue(choice);
		        }
			}
		};
		final JButton bValue = new JButton(aValue);
		
//		final JPanel valueBox = new JPanel();
//		valueBox.setLayout(new BorderLayout());
//		valueBox.add(value, BorderLayout.CENTER);
//		valueBox.add(bValue, BorderLayout.EAST);
		
//		final JComboBox valueBox = new JComboBox();
//		valueBox.setEditable(true);
//		valueBox.setSelectedItem(l.getValue());
//		valueBox.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				l.setValue(valueBox.getSelectedItem().toString());
//			}
//		});

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
								.addComponent(value))
				.addGroup(
						layout.createParallelGroup().addComponent(bValue)));

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
								.addComponent(value)
								.addComponent(bValue)));
		return editor;
	}
	
	private class RestrictedFileSystemView extends FileSystemView{
		
		File rootDir;

		public RestrictedFileSystemView(File root)
		{
			super();
			rootDir = root;
		}
		
		public boolean isRoot(File f) {
			if (f.equals(rootDir)) return true;
			return false;
		}

		@Override
		public File getDefaultDirectory()
		{
			return rootDir;
		}

		@Override
		public File getHomeDirectory()
		{
			return rootDir;
		}

		@Override
		public File[] getRoots()
		{
			return new File[] {rootDir};
		}

		@Override
		public File createNewFolder(File containingDir) throws IOException {
			File dir = new File(containingDir, "New Folder");
			dir.mkdir();
			return dir;
		}
	}

}
