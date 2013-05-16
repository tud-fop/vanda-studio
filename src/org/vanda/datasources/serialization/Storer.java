package org.vanda.datasources.serialization;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.vanda.datasources.DataSource;
import org.vanda.datasources.RootDataSource;

import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public class Storer {
	
	private final List<DataSourceType<?>> types;
	
	public Storer(List<DataSourceType<?>> types) {
		this.types = types;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void store(RootDataSource w, String filename) throws Exception {
		Writer writer = new FileWriter(new File(filename));
		final PrettyPrintWriter ppw = new PrettyPrintWriter(writer);
		ppw.startNode("root");
		for (Map.Entry<String, DataSource> j : w.mtab()) {
			ppw.startNode("mount");
			ppw.addAttribute("path", j.getKey());
			DataSource ds = j.getValue();
			for (DataSourceType<? extends DataSource> dst : types) {
				// XXX use hash map
				if (ds.getClass().equals(dst.getDataSourceClass())) {
					((DataSourceType) dst).store(ppw, ds);
					break;
				}
			}
			ppw.endNode(); // mount
		}
		ppw.endNode(); // root
	}

}
