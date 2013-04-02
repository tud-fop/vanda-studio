package org.vanda.datasources;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.util.Observer;

public class RootDataSource implements DataSource {
	
	private Map<String, DataSource> sources;

	public RootDataSource(Map<String, DataSource> sources) {
		this.sources = sources;
	}

	final class RootElement implements ElementSelector, Observer<Element> {

		private String prefix;
		private Element element;
		private ElementSelector elementSelector;
		
		private List<String> dsList;

		private JComboBox jDSList;
		private JComponent component;
		private JPanel selector;

		public RootElement() {
			dsList = new ArrayList<String>(sources.keySet());
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
					notify(element);
				}
			}
		}

		@Override
		public void notify(Element event) {
			String prefix1 = event.getPrefix();
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
				selector.revalidate();
			}
		}
	}

	@Override
	public ElementSelector createSelector() {
		return new RootElement();
	}
	
	public void mount(String prefix, DataSource ds) {
		sources.put(prefix, ds);
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

}
