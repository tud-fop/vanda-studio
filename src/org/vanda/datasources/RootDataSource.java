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
			
			prefix = "";

			selector = new JPanel(new GridBagLayout());

			jDSList = new JComboBox(dsList.toArray());
			jDSList.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (element != null) {
						String prefix1 = jDSList.getSelectedItem().toString();
						element.setPrefix(prefix1);
					}
				}
			});
			
			component = new JPanel(new BorderLayout());
			
			Insets i = new Insets(2, 2, 2, 2);
			int anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;

			selector.add(jDSList, new GridBagConstraints(0, 0, 3, 1, 1, 0,
					anchor, GridBagConstraints.BOTH, i, 1, 1));
			
			selector.add(component, new GridBagConstraints(0, 2, 3, 1, 1, 1,
					anchor, GridBagConstraints.BOTH, i, 1, 1));
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
				component.removeAll();
				if (elementSelector != null)
					elementSelector.setElement(null);
				DataSource ds = sources.get(prefix);
				if (ds != null) {
					elementSelector = ds.createSelector();
					elementSelector.setElement(element);
					component.add(elementSelector.getComponent(), BorderLayout.CENTER);
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
				/**
				 * 
				 */
				private static final long serialVersionUID = 7064196111301292429L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int num = 1;
					String prefix = "New DataSource ";
					while (sources.containsKey(prefix + num))
						num++;
					String id = prefix + num;
					DataSourceFactory type = (DataSourceFactory) JOptionPane
							.showInputDialog(editor.getParent(), "Choose a Class",
									"Data Source Class",
									JOptionPane.INFORMATION_MESSAGE, null,
									getItems().toArray(),
									getItems().toArray()[0]);
					if (type != null) {
						mount(id, type.getDataSource());
						notifyMe(id);
					}
				}
			});
			JButton bRemove = new JButton(new AbstractAction("remove") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 2452009339950565899L;

				@Override
				public void actionPerformed(ActionEvent e) {
					umount((String) lDataSources.getSelectedValue());
					notifyMe();
				}
			});
			JButton bSave = new JButton(new AbstractAction("save") {
				/**
				 * 
				 */
				private static final long serialVersionUID = -6511909543313052584L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					writeChange();
				}
			});
			JButton bCancel = new JButton(new AbstractAction("cancel") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 6801109424796554027L;

				@Override
				public void actionPerformed(ActionEvent e) {
					notifyMe();
				}
			});
			JLabel lId = new JLabel("ID");

			Insets i = new Insets(2, 2, 2, 2);
			int anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;

			editor.add(lDataSources, new GridBagConstraints(0, 0, 3, 2, 6, 4,
					anchor, GridBagConstraints.BOTH, i, 1, 1));
			editor.add(bNew, new GridBagConstraints(0, 2, 1, 1, 3, 0, anchor,
					GridBagConstraints.BOTH, i, 1, 1));
			editor.add(bRemove, new GridBagConstraints(2, 2, 1, 1, 3, 0,
					anchor, GridBagConstraints.BOTH, i, 1, 1));
			editor.add(lId, new GridBagConstraints(3, 0, 1, 1, 3, 0, anchor,
					GridBagConstraints.BOTH, i, 1, 1));
			editor.add(aId, new GridBagConstraints(4, 0, 1, 1, 3, 0, anchor,
					GridBagConstraints.BOTH, i, 1, 1));
			editor.add(innerEditorPanel, new GridBagConstraints(3, 1, 2, 1, 6,
					3, anchor, GridBagConstraints.BOTH, i, 1, 1));
			editor.add(bSave, new GridBagConstraints(3, 2, 1, 1, 3, 0, anchor,
					GridBagConstraints.BOTH, i, 1, 1));
			editor.add(bCancel, new GridBagConstraints(4, 2, 1, 1, 3, 0,
					anchor, GridBagConstraints.BOTH, i, 1, 1));
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
			notifyMe(lDataSources.getSelectedValue());
		}
		private void notifyMe(Object id) {
			Object[] a = sources.keySet().toArray();
			Arrays.sort(a);
			lDataSources.setListData(a);
			int idx = -1;
			for (int i = 0; i < a.length; ++i) {
				if (a[i].equals(id)) {
					idx = i;
					break;
				}
			}
			lDataSources.setSelectedIndex(idx);
		}

		@Override
		public void write() {
			if (innerEditor != null) {
				innerEditor.writeChange();
				sources.remove(lDataSources.getSelectedValue());
				sources.put(aId.getText(), innerEditor.getDataSource());
			}
			notifyMe(aId.getText());
		}

	}

	@Override
	public DataSourceEditor createEditor(Application app) {
		return new RootDataSourceEditor(app);
	}

}
