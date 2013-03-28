package org.vanda.datasources;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.vanda.studio.app.Application;
import org.vanda.types.Type;

public class DirectoryDataSourceFactory extends DataSourceFactory {

	private File dir;
	private String filter;
	private Type type;

	public DirectoryDataSourceFactory(Application app, String name, Type type,
			String path, String filter) {
		super(app, name);
		this.type = type;
		this.filter = filter;
		this.dir = new File(app.findFile(path));
	}

	public class DirectoryDataSource extends DataSource {

		private JList selector;

		public DirectoryDataSource(String name) {
			super(name);
			selector = new JList();
			selector.setListData(dir.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(filter);
				}
			}));
			selector.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					observers.notify(DirectoryDataSource.this);
				}
			});
		}

		@Override
		public JComponent getElementSelector() {
			return selector;
		}

		@Override
		public String getSelectedElement() {
			return getName() + ":" + selector.getSelectedValue();
		}

		@Override
		public void setSelectedElement(String element) {
			selector.setSelectedValue(element.split(":")[1], true);
		}

		@Override
		public String getValue(String element) {
			return dir.getAbsolutePath() + "/" + element.split(":")[1];
		}

		@Override
		public Type getType(String element) {
			return type;
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
		return "Directory Data Source.";
	}

	@Override
	public String getName() {
		return "DirectoryDataSource";
	}

	@Override
	public String getVersion() {
		return "2012-03-28";
	}

	@Override
	public DataSource createInstance() {
		return new DirectoryDataSource(getPrefix());
	}

}
