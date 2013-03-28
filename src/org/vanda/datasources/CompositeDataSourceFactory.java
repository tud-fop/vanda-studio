package org.vanda.datasources;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.vanda.studio.app.Application;
import org.vanda.types.Type;
import org.vanda.util.MetaRepository;

public class CompositeDataSourceFactory extends DataSourceFactory {

	public CompositeDataSourceFactory(Application app) {
		super(app, "cds");
	}

	final class CompositeDataSource extends DataSource {

		private List<String> dsList;
		private Map<String, DataSource> dsMap;

		private JComboBox jDSList;
		private JComponent jDataSource;
		private JPanel selector;

		public CompositeDataSource(String name,
				MetaRepository<DataSourceFactory> mr) {
			super(name);
			Collection<DataSourceFactory> col = mr.getRepository().getItems();
			dsList = new ArrayList<String>();
			dsMap = new HashMap<String, DataSource>(col.size());
			for (DataSourceFactory dsf : col) {
				DataSource ds = dsf.createInstance();
				ds.getObservable().addObserver(observers);
				dsList.add(ds.getName());
				dsMap.put(ds.getName(), ds);
			}

			Collections.sort(dsList);

			selector = new JPanel();
			selector.setLayout(new BorderLayout());
			jDSList = new JComboBox(dsList.toArray());
			selector.add(jDSList, BorderLayout.NORTH);
			jDataSource = new JPanel();
			selector.add(jDataSource, BorderLayout.CENTER);
			jDSList.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selector.remove(jDataSource);
					jDataSource = dsMap.get(jDSList.getSelectedItem())
							.getElementSelector();
					selector.add(jDataSource, BorderLayout.CENTER);
					selector.revalidate();
				}
			});
			jDSList.setSelectedIndex(0);
		}

		@Override
		public JComponent getElementSelector() {
			return selector;
		}

		@Override
		public String getSelectedElement() {
			return dsMap.get(jDSList.getSelectedItem()).getSelectedElement();
		}

		@Override
		public void setSelectedElement(String element) {
			if (!element.isEmpty()) {
				String dsName = element.split(":")[0];
				dsMap.get(dsName).setSelectedElement(element);
				jDSList.setSelectedItem(dsName);
			}
		}

		@Override
		public String getValue(String element) {
			return dsMap.get(element.split(":")[0]).getValue(element);
		}

		@Override
		public Type getType(String element) {
			return dsMap.get(element.split(":")[0]).getType(element);
		}
	}

	@Override
	public String getCategory() {
		return "Data Sources";
	}

	@Override
	public String getContact() {
		return "Tobias.Denkinger@mailbox.tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "Encapsules multiple Data Sources.";
	}

	@Override
	public String getName() {
		return "CompositeDataSource";
	}

	@Override
	public String getVersion() {
		return "2012-03-27";
	}

	@Override
	public CompositeDataSource createInstance() {
		return new CompositeDataSource(getPrefix(),
				app.getDataSourceMetaRepository());
	}

}
