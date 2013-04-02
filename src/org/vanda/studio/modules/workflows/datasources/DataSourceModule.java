package org.vanda.studio.modules.workflows.datasources;

import org.vanda.datasources.DirectoryDataSource;
import org.vanda.datasources.IntegerDataSource;
import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.types.CompositeType;

public class DataSourceModule implements Module {

	@Override
	public String getName() {
		return "Data Sources";
	}

	@Override
	public Object createInstance(Application a) {
		RootDataSource rds = a.getRootDataSource();
		rds.mount("Integer", new IntegerDataSource());
		rds.mount("europarl", new DirectoryDataSource(
				new CompositeType("SentenceCorpus"), "europarl_eng-ger", ".*"));
		rds.mount("example1", new DirectoryDataSource(
				new CompositeType("SentenceCorpus"), "example_eng-spa", ".*"));
		rds.mount("example2", new DirectoryDataSource(
				new CompositeType("SentenceCorpus"), "example_arc-cen", ".*"));
		rds.mount("example3", new DirectoryDataSource(
				new CompositeType("SentenceCorpus"), "example_astronauts", ".*"));
		rds.mount("Berkeley Grammars", new DirectoryDataSource(
				new CompositeType("BerkeleyGrammar.sm6"), "grammars", ".*gr"));
		rds.mount("LAPCFG Grammars", new DirectoryDataSource(
				new CompositeType("LAPCFG-Grammar"), "lapcfg", ".*"));
		return null;
	}

}
