package org.vanda.studio.modules.workflows.inspector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.types.Type;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.hyper.MutableWorkflow;

public class LiteralEditor implements ElementEditorFactory<Literal> {

	public class FileChooserPreview extends JPanel implements
			PropertyChangeListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8680540754076190091L;
		private JFileChooser fc;
		private PreviewFactory pf;
		private JComponent c;

		public FileChooserPreview(JFileChooser jfc, PreviewFactory pf) {
			super();
			fc = jfc;
			this.pf = pf;
			refresh(null);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getNewValue() instanceof File)
				refresh((File) evt.getNewValue());
		}

		public void refresh(File f) {
			if (c != null)
				remove(c);
			if (f == null) {
				c = new JTextArea("no file selected");
			} else {
				c = pf.createSmallPreview(f.getAbsolutePath());
			}
			add(c, BorderLayout.CENTER);
			if (c.getPreferredSize().width * 2 > fc.getPreferredSize().width
					|| c.getPreferredSize().height * 2 > fc.getPreferredSize().height)
				fc.setPreferredSize(new Dimension(c.getPreferredSize().width * 2,
						c.getPreferredSize().height * 2));
			c.revalidate();
			fc.revalidate();
		}

	}

	@Override
	public JComponent createEditor(final Application app, MutableWorkflow wf,
			Token address, final Literal l) {
		final JLabel label1 = new JLabel("Type:");
		final JLabel label2 = new JLabel("Value:");
		final List<Type> types = new ArrayList<Type>();
		final JComboBox typeBox = new JComboBox();
		for (Type t : app.getTypes())
			types.add(t);
		Collections.sort(types, new Comparator<Type>() {
			@Override
			public int compare(Type o1, Type o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		typeBox.setModel(new DefaultComboBoxModel(types.toArray()));
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
				String prefix1 = app.getProperty("inputPath");
				String prefix2 = app.getProperty("outputPath");
				File f = new File(app.getProperty("lastInputPath"));
				FileSystemView rfsv = new RestrictedFileSystemView(f);
				JFileChooser fc = new JFileChooser(rfsv);
				FileChooserPreview fcp = new FileChooserPreview(fc,
						app.getPreviewFactory((Type) typeBox.getSelectedItem()));
				fc.setAccessory(fcp);
				fc.addPropertyChangeListener(fcp);
				int returnVal = fc.showOpenDialog(null);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String choice = fc.getSelectedFile().getAbsolutePath()
							.replaceFirst(prefix1, "").replaceFirst(prefix2, "");
					value.setText(choice);
					l.setValue(choice);
					app.setProperty("lastInputPath", fc.getSelectedFile()
							.getParent());
				}
			}
		};
		final JButton bValue = new JButton(aValue);

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
				.addGroup(layout.createParallelGroup().addComponent(bValue)));

		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(label1).addComponent(typeBox))
				.addGroup(
						layout.createParallelGroup(
								GroupLayout.Alignment.BASELINE)
								.addComponent(label2).addComponent(value)
								.addComponent(bValue)));
		return editor;
	}

	private class RestrictedFileSystemView extends FileSystemView {

		File rootDir;

		public RestrictedFileSystemView(File root) {
			super();
			rootDir = root;
		}

		@Override
		public File getDefaultDirectory() {
			return rootDir;
		}

		@Override
		public File getHomeDirectory() {
			return rootDir;
		}

		@Override
		public File createNewFolder(File containingDir) throws IOException {
			File dir = new File(containingDir, "New Folder");
			dir.mkdir();
			return dir;
		}
	}

}
