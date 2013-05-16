package org.vanda.datasources.serialization;

import org.vanda.datasources.DataSource;
import org.vanda.xml.SingleElementHandlerFactory;

import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public interface DataSourceType<DS extends DataSource> {
	Class<DS> getDataSourceClass();
	SingleElementHandlerFactory<MountBuilder> load();
	void store(PrettyPrintWriter ppw, DS ds);

}
