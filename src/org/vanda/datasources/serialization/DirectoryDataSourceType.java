package org.vanda.datasources.serialization;

import org.vanda.datasources.DirectoryDataSource;
import org.vanda.xml.SimpleElementHandlerFactory;
import org.vanda.xml.SingleElementHandlerFactory;

import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public class DirectoryDataSourceType implements DataSourceType<DirectoryDataSource> {

	@Override
	public Class<DirectoryDataSource> getDataSourceClass() {
		return DirectoryDataSource.class;
	}

	@Override
	public SingleElementHandlerFactory<MountBuilder> load() {
		return new SimpleElementHandlerFactory<MountBuilder, DirectoryDataSourceBuilder>("directory", null,
				DirectoryDataSourceBuilder.createFactory(), MountBuilder.dsp, DirectoryDataSourceBuilder.createProcessor(),
				null);
	}

	@Override
	public void store(PrettyPrintWriter ppw, DirectoryDataSource ds) {
		ppw.startNode("directory");
		ppw.addAttribute("path", ds.dir.getPath());
		ppw.addAttribute("filter", ds.filter);
		ppw.addAttribute("type", ds.type.toString());
		ppw.endNode();
	}

}
