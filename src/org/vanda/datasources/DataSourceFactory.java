package org.vanda.datasources;

import org.vanda.studio.app.Application;
import org.vanda.util.RepositoryItem;

public abstract class DataSourceFactory implements RepositoryItem {

	Application app;
	String prefix;
	
	public DataSourceFactory(Application app, String prefix) {
		this.app = app;
		this.prefix = prefix;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	abstract public DataSource createInstance();
	
	@Override
	public String getId() {
		return prefix;
	}


}
