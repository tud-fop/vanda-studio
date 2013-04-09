package org.vanda.datasources;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.vanda.datasources.Elements.ElementEvent;
import org.vanda.datasources.Elements.ElementListener;
import org.vanda.types.Type;
import org.vanda.types.Types;
import org.vanda.util.Observer;

public class RootDataSource implements DataSource {
	
	private Map<String, DataSource> sources;

	public RootDataSource(Map<String, DataSource> sources) {
		this.sources = sources;
	}

	final class RootElement implements ElementSelector, Observer<ElementEvent<Element>>, ElementListener<Element> {

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

}
