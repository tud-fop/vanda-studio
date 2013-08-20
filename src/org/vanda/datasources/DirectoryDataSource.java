package org.vanda.datasources;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.datasources.Elements.ElementEvent;
import org.vanda.datasources.Elements.ElementListener;
import org.vanda.studio.app.Application;
import org.vanda.types.Type;
import org.vanda.util.Observer;

public class DirectoryDataSource implements DataSource {

	public File dir;
	public String filter;
	public Type type;

	public DirectoryDataSource(Type type, String path, String filter) {
		this.type = type;
		this.filter = filter;
		this.dir = new File(path);
	}

	public class DirectoryElementSelector implements ElementSelector,
			Observer<ElementEvent<Element>>, ElementListener<Element> {

		private Element element;
		private JList selector;
		private JScrollPane scroll;

		public DirectoryElementSelector() {
			selector = new JList();
			String[] l = dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File _, String name) {
					return name.matches(filter);
				}
			});
			if (l != null)
				selector.setListData(l);
			selector.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (element != null)
						element.setValue(selector.getSelectedValue().toString());
				}
			});
			scroll = new JScrollPane(selector);
		}

		@Override
		public JComponent getComponent() {
			return scroll;
		}

		@Override
		public Element getElement() {
			return element;
		}

		@Override
		public void setElement(Element e) {
			if (element != e) {
				if (element != null)
					element.getObservable().removeObserver(this);
				element = e;
				if (element != null) {
					element.getObservable().addObserver(this);
					valueChanged(element);
				}
			}
		}

		@Override
		public void notify(ElementEvent<Element> event) {
			event.doNotify(this);
		}

		@Override
		public void prefixChanged(Element e) {
			// do nothing
		}

		@Override
		public void valueChanged(Element e) {
			selector.setSelectedValue(e.getValue(), true);
		}
	}

	@Override
	public ElementSelector createSelector() {
		return new DirectoryElementSelector();
	}

	@Override
	public String getValue(Element element) {
		return dir.getAbsolutePath() + "/" + element.getValue();
	}

	@Override
	public Type getType(Element element) {
		return type;
	}

	public class DirectoryDataSourceEditor extends DataSourceEditor {

		private JPanel pan;
		private JTextField tFolder;
		private JTextField tFilter;
		private JComboBox cType;
		

		public DirectoryDataSourceEditor(Application app) {
			pan = new JPanel(new GridBagLayout());
			JLabel lFolder = new JLabel("Folder", JLabel.TRAILING);
			tFolder = new JTextField(dir.getAbsolutePath());
			JButton bFolder = new JButton(new AbstractAction("..."){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JFileChooser fc = new JFileChooser(new File(tFolder.getText()));
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
			            tFolder.setText(file.getAbsolutePath());
			        }
				}
			});
			JLabel lFilter = new JLabel("Filter", JLabel.TRAILING);
			tFilter = new JTextField(filter);
			JLabel lType = new JLabel("Type", JLabel.TRAILING);
			cType = new JComboBox(app.getTypes().toArray());
			cType.setSelectedItem(type);

			pan.add(tFolder);
			pan.add(tFilter);
			
			pan.add(cType);

			GridBagConstraints gbc;
			gbc = new GridBagConstraints(0, 0, 1, 1, 0, 0,
					GridBagConstraints.ABOVE_BASELINE_LEADING,
					GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0);
			
			pan.add(lFolder, gbc);
			
			gbc.gridy = 1;
			pan.add(lFilter, gbc);
			
			gbc.gridy = 2;
			pan.add(lType, gbc);
			
			gbc.gridy = 0;
			gbc.gridx = 1;
			gbc.weightx = 1;
			pan.add(tFolder, gbc);
			
			gbc.gridx = 2;
			gbc.weightx = 0;
			pan.add(bFolder, gbc);
			
			gbc.gridwidth = 2;
			gbc.weightx = 1;
			gbc.gridx = 1;
			gbc.gridy = 1;
			pan.add(tFilter, gbc);
			
			gbc.gridy = 2;
			pan.add(cType, gbc);
		}

		@Override
		public JComponent getComponent() {
			return pan;
		}

		@Override
		public DataSource getDataSource() {
			return DirectoryDataSource.this;
		}

		@Override
		public void write() {
			dir = new File(tFolder.getText());
			filter = tFilter.getText();
			type = (Type) cType.getSelectedItem();
		}

	}

	@Override
	public DataSourceEditor createEditor(Application app) {
		return new DirectoryDataSourceEditor(app);
	}

}
