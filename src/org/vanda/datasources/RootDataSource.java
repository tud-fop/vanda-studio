package org.vanda.datasources;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
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

		private JComboBox<Object> jDSList;
		private JComponent component;
		private JPanel selector;

		public RootElement() {
			dsList = new ArrayList<String>(sources.keySet());
			dsList.add("");
			Collections.sort(dsList);
			
			prefix = "";

			selector = new JPanel(new GridBagLayout());

			jDSList = new JComboBox<Object>(dsList.toArray());
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
		if (element == null)
			return null;
		DataSource ds = sources.get(element.getPrefix());
		return ds != null ? ds.getType(element) : Types.undefined;
	}

	@Override
	public String getValue(Element element) {
		DataSource ds = sources.get(element.getPrefix());
		return ds != null ? ds.getValue(element) : "";
	}

	public class RootDataSourceEditor extends DataSourceEditor {

		private JSplitPane editor;
		private JPanel sourceSelectionPanel;
		private JPanel sourceEditPanel;
		private DataSourceEditor innerEditor;
		private JPanel innerEditorPanel;
		private JList<Object> lDataSources;
		private int lastIndex = -1;
		private boolean switchingBack = false;

		public RootDataSourceEditor(final Application app) {
			sourceSelectionPanel = new JPanel(new GridBagLayout());
			sourceEditPanel = new JPanel(new GridBagLayout());
			editor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sourceSelectionPanel, sourceEditPanel);
			innerEditorPanel = new JPanel(new GridLayout(1, 1));
			lDataSources = new JList<Object>();
			resetEntry();
			lDataSources.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					if (switchingBack) {
						switchingBack = false;
						return;
					}
					if (askToGoBack("switch to another entry")) {
						switchingBack = true;
						lDataSources.setSelectedIndex(lastIndex);
						return;
					}
					lastIndex = lDataSources.getSelectedIndex();
					innerEditorPanel.removeAll();
					if (lDataSources.getSelectedIndex() > -1) {
						innerEditor = sources.get(
								lDataSources.getSelectedValue()).createEditor(app);
						innerEditorPanel.add(innerEditor.getComponent());

					}
					editor.revalidate();
				}
			});
			
			// Try to use fancy icons for the buttons
			Icon plusIcon, minusIcon;
			String plusText="", minusText="";
			try {
				Image plusImage, minusImage;
				plusImage = ImageIO.read(ClassLoader.getSystemClassLoader().getResource("plus.png"));
				minusImage = ImageIO.read(ClassLoader.getSystemClassLoader().getResource("minus.png"));
				plusIcon = new ImageIcon(plusImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
				minusIcon = new ImageIcon(minusImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
			} catch (Exception e) {
				plusIcon = null;
				minusIcon = null;
				plusText = "+";
				minusText = "-";
			}
			
			JButton bAdd = new JButton(new AbstractAction(plusText, plusIcon) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 7064196111301292429L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (askToGoBack("add a new item"))
						return;
					// Auto-generate a name...
					int num = 1;
					String prefix = "New DataSource ";
					while (sources.containsKey(prefix + num))
						num++;
					String id = prefix + num;
					// ...but prefer one chosen by the user
					id = (String) JOptionPane.showInputDialog(
							editor,
							"Enter a name:",
							"New data source",
							JOptionPane.PLAIN_MESSAGE,
							null,
							null,
							id);
					// Canceling the naming dialog shouldn't screw the process up
					if (id == null)
						id = prefix + num;
					DataSourceFactory type = (DataSourceFactory) JOptionPane
							.showInputDialog(editor.getParent(), "Choose a Class",
									"Data Source Class",
									JOptionPane.INFORMATION_MESSAGE, null,
									getItems().toArray(),
									getItems().toArray()[0]);
					if (type != null) {
						mount(id, type.getDataSource());
						resetEntry(id);
						writeChange();
					}
				}
			});
			JButton bRemove = new JButton(new AbstractAction(minusText, minusIcon) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 2452009339950565899L;

				@Override
				public void actionPerformed(ActionEvent e) {
					umount((String) lDataSources.getSelectedValue());
					resetEntry();
					innerEditorPanel.removeAll();
					innerEditor = null;
					writeChange();
				}
			});
			JButton bRename = new JButton(new AbstractAction("rename") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 5152467568739410638L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (askToGoBack("rename this source"))
						return;
					String s = (String) JOptionPane.showInputDialog(
		                    editor,
		                    "Enter the new name:",
		                    "Rename data source",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    null,
		                    lDataSources.getSelectedValue().toString());
					if(s != null) {
						sources.remove(lDataSources.getSelectedValue());
						sources.put(s, innerEditor.getDataSource());
						resetEntry(s);
					}
				}
			});
			
			JButton bSave = new JButton(new AbstractAction("save source") {
				/**
				 * 
				 */
				private static final long serialVersionUID = -6511909543313052584L;

				@Override
				public void actionPerformed(ActionEvent arg0) {
					writeChange();
				}
			});
			JButton bReset = new JButton(new AbstractAction("reset source") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 6801109424796554027L;

				@Override
				public void actionPerformed(ActionEvent e) {
					if (!askToGoBack("reset this source"))
						resetEntry();
				}
			});
			
			
			
			
			Insets i = new Insets(3, 3, 3, 3);
			int anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;

			sourceSelectionPanel.add(lDataSources, new GridBagConstraints(
					0, 0, 3, 1, 6, 4, anchor, GridBagConstraints.BOTH, i, 1, 1));
			sourceSelectionPanel.add(bAdd, new GridBagConstraints(
					0, 1, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));
			sourceSelectionPanel.add(bRemove, new GridBagConstraints(
					1, 1, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));
			sourceSelectionPanel.add(bRename, new GridBagConstraints(
					2, 1, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));

			sourceEditPanel.add(innerEditorPanel, new GridBagConstraints(
					0, 0, 2, 1, 6, 3, anchor, GridBagConstraints.BOTH, i, 1, 1));
			sourceEditPanel.add(bSave, new GridBagConstraints(
					0, 1, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));
			sourceEditPanel.add(bReset, new GridBagConstraints(
					1, 1, 1, 1, 3, 0, anchor, GridBagConstraints.BOTH, i, 1, 1));
		}

		@Override
		public JComponent getComponent() {
			return editor;
		}

		@Override
		public DataSource getDataSource() {
			return innerEditor.getDataSource();
		}

		private void resetEntry() {
			resetEntry(lDataSources.getSelectedValue());
		}
		private void resetEntry(Object id) {
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

		public boolean askToGoBack(String whatdo) {
			if (innerEditor != null && innerEditor.wasChanged()) {
				int q = JOptionPane.showOptionDialog(editor, "Despite having changed, but not saved the properties\n"
						+ "of a data source, in the editor, you want to "+whatdo+" .\n"
						+ "Do you really want to do that?", "Unsaved changes",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
				
				return q != 0;
			}
			return false;
		}

		@Override
		public void write() {
			if (innerEditor != null) {
				innerEditor.writeChange();
				sources.remove(lDataSources.getSelectedValue());
				sources.put(lDataSources.getSelectedValue().toString(), innerEditor.getDataSource());
			}
			resetEntry(lDataSources.getSelectedValue());
		}

	}

	@Override
	public DataSourceEditor createEditor(Application app) {
		return new RootDataSourceEditor(app);
	}

}
