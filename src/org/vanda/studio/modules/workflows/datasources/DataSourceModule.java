package org.vanda.studio.modules.workflows.datasources;

import org.vanda.datasources.DataSourceFactory;
import org.vanda.datasources.DirectoryDataSourceFactory;
import org.vanda.datasources.IntegerSourceFactory;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.types.CompositeType;
import org.vanda.util.ListRepository;
import org.vanda.util.MetaRepository;

public class DataSourceModule implements Module {

	@Override
	public String getName() {
		return "Data Sources";
	}

	@Override
	public Object createInstance(Application a) {
		MetaRepository<DataSourceFactory> dsr = a.getDataSourceMetaRepository();
		ListRepository<DataSourceFactory> r = new ListRepository<DataSourceFactory>();
		r.addItem(new IntegerSourceFactory(a));
		r.addItem(new DirectoryDataSourceFactory(a, "europarl",
				new CompositeType("SentenceCorpus"), "europarl_eng-ger", ".*"));
		r.addItem(new DirectoryDataSourceFactory(a, "example1",
				new CompositeType("SentenceCorpus"), "example_eng-spa", ".*"));
		r.addItem(new DirectoryDataSourceFactory(a, "example2",
				new CompositeType("SentenceCorpus"), "example_arc-cen", ".*"));
		r.addItem(new DirectoryDataSourceFactory(a, "example3",
				new CompositeType("SentenceCorpus"), "example_astronauts", ".*"));
		r.addItem(new DirectoryDataSourceFactory(a, "Berkeley Grammars",
				new CompositeType("BerkeleyGrammar.sm6"), "grammars", ".*gr"));
		r.addItem(new DirectoryDataSourceFactory(a, "LAPCFG Grammars",
				new CompositeType("LAPCFG-Grammar"), "lapcfg", ".*"));
		dsr.addRepository(r);
		return null;
	}

}
