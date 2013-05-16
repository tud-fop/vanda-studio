package org.vanda.datasources;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.datasources.Elements.ElementEvent;
import org.vanda.datasources.Elements.ElementListener;
import org.vanda.studio.app.Application;
import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.util.ListRepository;
import org.vanda.util.Observer;

public class RootDataSource extends ListRepository<DataSourceFactory> implements
		DataSource {

	private Map<String, DataSource> sources;

	public RootDataSource(Map<String, DataSource> sources) {
		super();
		this.sources = sources;
	}

	final class RootElement implements ElementSelector,
			Observer<ElementEvent<Element>>, ElementListener<Element> {

		private String prefix;
		private Element element;
		private ElementSelector elementSelector;

		private List<String> dsList;

		private JComboBox jDSList;
		private JComponent component;
		private JPanel selector;

		public RootElement() {
			dsList = new ArrayList<String>(sources.keySet());
			dsList.add("");
			Collections.sort(dsList);

			selector = new JPanel();
			selector.setLayout(new BorderLayout());
			jDSList = new JComboBox(dsList.toArray());
			selector.add(jDSList, BorderLayout.NORTH);
			component = new JPanel();
			selector.add(component, BorderLayout.CENTER);
			jDSList.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (element != null) {
						String prefix1 = jDSList.getSelectedItem().toString();
						element.setPrefix(prefix1);
					}
				}
			});
			prefix = "";
			// jDSList.setSelectedIndex(0);
		}

		@Override
		public JComponent getComponent() {
			return selector;
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
					prefixChanged(element);
				}
			}
		}

		@Override
		public void prefixChanged(Element e) {
			String prefix1 = e.getPrefix();
			if (!prefix.equals(prefix1)) {
				prefix = prefix1;
				jDSList.setSelectedItem(prefix);
				selector.remove(component);
				if (elementSelector != null)
					elementSelector.setElement(null);
				DataSource ds = sources.get(prefix);
				if (ds != null) {
					elementSelector = ds.createSelector();
					elementSelector.setElement(element);
					component = elementSelector.getComponent();
					selector.add(component, BorderLayout.CENTER);
				} else
					elementSelector = null;
				selector.validate();
			}
		}

		@Override
		public void valueChanged(Element e) {
			// do nothing
		}

		@Override
		public void notify(ElementEvent<Element> event) {
			event.doNotify(this);
		}
	}

	@Override
	public ElementSelector createSelector() {
		return new RootElement();
	}

	public void mount(String prefix, DataSource ds) {
		sources.put(prefix, ds);
	}

	public Set<Map.Entry<String, DataSource>> mtab() {
		return sources.entrySet();
	}

	public void umount(String prefix) {
		sources.remove(prefix);
	}

	@Override
	public Type getType(Element element) {
		DataSource ds = sources.get(element.getPrefix());
		return ds != null ? ds.getType(element) : Types.undefined;
	}

	@Override
	public String getValue(Element element) {
		DataSource ds = sources.get(element.getPrefix());
		return ds != null ? ds.getValue(element) : "";
	}

	public class RootDataSourceEditor extends DataSourceEditor {

		private JPanel editor;
		private DataSourceEditor innerEditor;
		private JPanel innerEditorPanel;
		private JTextField aId;
		private JList lDataSources;

		public RootDataSourceEditor(final Application app) {
			editor = new JPanel(new GridBagLayout());
			innerEditorPanel = new JPanel(new GridLayout(1, 1));
			aId = new JTextField();
			lDataSources = new JList();
			notifyMe();
			lDataSources.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					innerEditorPanel.removeAll();
					aId.setText("");
					if (lDataSources.getSelectedIndex() > -1) {
						innerEditor = sources.get(
								lDataSources.getSelectedValue()).createEditor(
								app);
						innerEditorPanel.add(innerEditor.getComponent());
						aId.setText((String) lDataSources.getSelectedValue());

					}
					editor.revalidate();
				}
			});
			JButton bNew = new JButton(new AbstractAction("new") {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					int num = 1;
					String prefix = "New DataSource ";
					while (sources.containsKey(prefix + num))
						num++;
					String id = prefix + num;
					DataSourceFactory type = (DataSourceFactory) JOptionPane
							.showInputDialog(null, "Choose a Class",
									"Data Source Class",
									JOptionPane.INFORMATION_MESSAGE, null,
									getItems().toArray(),
									getItems().toArray()[0]);
					if (type != null) {
						mount(id, type.getDataSource());
						notifyMe();
					}
				}
			});
			JButton bRemove = new JButton(new AbstractAction("remove") {
				@Override
				public void actionPerformed(ActionEvent e) {
					umount((String) lDataSources.getSelectedValue());
					notifyMe();
				}
			});
			JButton bSave = new JButton(new AbstractAction("save") {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					writeChange();
				}
			});
			JButton bCancel = new JButton(new AbstractAction("cancel") {
				@Override
				public void actionPerformed(ActionEvent e) {
					notifyMe();
				}
			});
			JLabel lId = new JLabel("ID");

			Insets i = new Insets(2, 2, 2, 2);
			int anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
			GridBagConstraints gbc;

			gbc = new GridBagConstraints(0, 0, 3, 2, 6, 4, anchor,
					GridBagConstraints.BOTH, i, 1, 1);
			editor.add(lDataSources, gbc);

			gbc = new GridBagConstraints(0, 2, 1, 1, 3, 0, anchor,
					GridBagConstraints.BOTH, i, 1, 1);
			editor.add(bNew, gbc);

			gbc = new GridBagConstraints(2, 2, 1, 1, 3, 0, anchor,
					GridBagConstraints.BOTH, i, 1, 1);
			editor.add(bRemove, gbc);

			gbc = new GridBagConstraints(3, 0, 1, 1, 3, 0, anchor,
					GridBagConstraints.BOTH, i, 1, 1);
			editor.add(lId, gbc);

			gbc = new GridBagConstraints(4, 0, 1, 1, 3, 0, anchor,
					GridBagConstraints.BOTH, i, 1, 1);
			editor.add(aId, gbc);

			gbc = new GridBagConstraints(3, 1, 2, 1, 6, 3, anchor,
					GridBagConstraints.BOTH, i, 1, 1);
			editor.add(innerEditorPanel, gbc);

			gbc = new GridBagConstraints(3, 2, 1, 1, 3, 0, anchor,
					GridBagConstraints.BOTH, i, 1, 1);
			editor.add(bSave, gbc);

			gbc = new GridBagConstraints(4, 2, 1, 1, 3, 0, anchor,
					GridBagConstraints.BOTH, i, 1, 1);
			editor.add(bCancel, gbc);
		}

		@Override
		public JComponent getComponent() {
			return editor;
		}

		@Override
		public DataSource getDataSource() {
			return innerEditor.getDataSource();
		}

		private void notifyMe() {
			int idx = lDataSources.getSelectedIndex();
			Object[] a = sources.keySet().toArray();
			Arrays.sort(a);
			lDataSources.setListData(a);
			if (idx < lDataSources.getModel().getSize())
				lDataSources.setSelectedIndex(idx);
			else
				lDataSources.setSelectedIndex(-1);
		}

		@Override
		public void write() {
			if (innerEditor != null) {
				innerEditor.writeChange();
				sources.remove(lDataSources.getSelectedValue());
				sources.put(aId.getText(), innerEditor.getDataSource());
			}
			notifyMe();
		}

	}

	@Override
	public DataSourceEditor createEditor(Application app) {
		return new RootDataSourceEditor(app);
	}

}
