package org.vanda.datasources;

import org.vanda.types.Type;

public class DirectoryDataSourceFactory implements DataSourceFactory {

	private String path, filter;
	private Type type;

	public DirectoryDataSourceFactory(String p, String f, Type t) {
		path = p;
		filter = f;
		type = t;
	}
	
	@Override
	public DataSource getDataSource() {
		return new DirectoryDataSource(type, path, filter);
	}

	@Override
	public String getCategory() {
		return "DataSourceFactory";
	}

	@Override
	public String getContact() {
		return "Tobias.Denkinger@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "Creates DirectoryDataSources.";
	}

	@Override
	public String getId() {
		return "DirectoryDataSourceFactory";
	}

	@Override
	public String getName() {
		return "Directory DataSource";
	}

	@Override
	public String getVersion() {
		return "2013-04-22";
	}

	@Override
	public String toString() {
		return getName();
	}
}
