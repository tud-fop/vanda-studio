package org.vanda.studio.modules.datasources;

import java.util.LinkedList;
import java.util.List;

import org.vanda.datasources.IntegerDataSource;
import org.vanda.datasources.RootDataSource;
import org.vanda.datasources.serialization.DataSourceType;
import org.vanda.datasources.serialization.DirectoryDataSourceType;
import org.vanda.datasources.serialization.Loader;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;

public class DataSourceModule implements Module {

	@Override
	public String getName() {
		return "Data Sources";
	}

	protected static String PROPERTIES_FILE = System.getProperty("user.home") + "/.vanda/datasources.xml";

	@Override
	public Object createInstance(Application a) {
		RootDataSource rds = a.getRootDataSource();
		List<DataSourceType<?>> dsts = new LinkedList<DataSourceType<?>>();
		dsts.add(new DirectoryDataSourceType());
		Loader l = new Loader(rds, dsts);
		try {
			l.load(PROPERTIES_FILE);
		} catch (Exception e) {
			// do nothing if file does not exist or so
		}

		rds.mount("Integer", new IntegerDataSource());
		/*
		 * rds.mount("europarl", new DirectoryDataSource( new
		 * CompositeType("SentenceCorpus"), "europarl_eng-ger", ".*"));
		 * rds.mount("example1", new DirectoryDataSource( new
		 * CompositeType("SentenceCorpus"), "example_eng-spa", ".*"));
		 * rds.mount("example2", new DirectoryDataSource( new
		 * CompositeType("SentenceCorpus"), "example_arc-cen", ".*"));
		 * rds.mount("example3", new DirectoryDataSource( new
		 * CompositeType("SentenceCorpus"), "example_astronauts", ".*"));
		 * rds.mount("Berkeley Grammars", new DirectoryDataSource( new
		 * CompositeType("BerkeleyGrammar.sm6"), "grammars", ".*gr"));
		 * rds.mount("LAPCFG Grammars", new DirectoryDataSource( new
		 * CompositeType("LAPCFG-Grammar"), "lapcfg", ".*"));
		 */
		return null;
	}

}
