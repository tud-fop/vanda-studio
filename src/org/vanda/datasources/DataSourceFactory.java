package org.vanda.datasources;

import org.vanda.util.RepositoryItem;

public interface DataSourceFactory extends RepositoryItem {
	DataSource getDataSource();
}
