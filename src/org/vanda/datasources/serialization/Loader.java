package org.vanda.datasources.serialization;

import java.io.File;
import java.util.List;

import org.vanda.datasources.RootDataSource;
import org.vanda.studio.app.Application;
import org.vanda.util.Observer;
import org.vanda.xml.ComplexFieldProcessor;
import org.vanda.xml.CompositeElementHandlerFactory;
import org.vanda.xml.ElementHandlerFactory;
import org.vanda.xml.Factory;
import org.vanda.xml.ParserImpl;
import org.vanda.xml.SimpleElementHandlerFactory;
import org.vanda.xml.SimpleRootHandler;
import org.vanda.xml.SingleElementHandlerFactory;

public class Loader {

	private final RootDataSource rds;
	private final List<DataSourceType<?>> types;
	private final Application app;

	public Loader(RootDataSource rds, List<DataSourceType<?>> types, final Application app) {
		this.rds = rds;
		this.types = types;
		this.app = app;
	}

	@SuppressWarnings("unchecked")
	private ElementHandlerFactory<MountBuilder> sourceHandler() {
		CompositeElementHandlerFactory<MountBuilder> cehf = new CompositeElementHandlerFactory<MountBuilder>();
		for (DataSourceType<?> dst : types)
			cehf.addHandler(dst.load());
		return cehf;
	}

	private ElementHandlerFactory<RootDataSource> mountHandler(ElementHandlerFactory<MountBuilder> sourceHandler) {
		ComplexFieldProcessor<RootDataSource, MountBuilder> xxx = new ComplexFieldProcessor<RootDataSource, MountBuilder>() {
			@Override
			public void process(RootDataSource rds1, MountBuilder mb) {
				rds1.mount(mb.prefix, mb.ds);
			}
		};
		return new SimpleElementHandlerFactory<RootDataSource, MountBuilder>("mount", sourceHandler,
				MountBuilder.createFactory(app), xxx, MountBuilder.createProcessor(), null);
	}

	private SingleElementHandlerFactory<Observer<RootDataSource>> rootHandler(
			ElementHandlerFactory<RootDataSource> mountHandler) {
		Factory<RootDataSource> xxx = new Factory<RootDataSource>() {
			@Override
			public RootDataSource create() {
				return rds;
			}
		};
		return new SimpleElementHandlerFactory<Observer<RootDataSource>, RootDataSource>("root", mountHandler, xxx,
				null, null, null);
	}

	private ParserImpl<RootDataSource> createParser() {
		ParserImpl<RootDataSource> p = new ParserImpl<RootDataSource>(null);
		p.setRootState(new SimpleRootHandler<RootDataSource>(p, rootHandler(mountHandler(sourceHandler()))));
		return p;
	}

	public void load(String filename) throws Exception {
		ParserImpl<RootDataSource> p = createParser();
		try {
			p.init(new File(filename));
			p.process();
		} finally {
			p.done();
		}
	}

}
