package org.vanda.workflows.serialization.run;

import java.io.File;

import org.vanda.studio.modules.workflows.run.RunConfig;
import org.vanda.util.Observer;
import org.vanda.xml.ComplexFieldProcessor;
import org.vanda.xml.CompositeElementHandlerFactory;
import org.vanda.xml.ParserImpl;
import org.vanda.xml.SimpleElementHandlerFactory;
import org.vanda.xml.SimpleRootHandler;
import org.vanda.xml.SingleElementHandlerFactory;

/**
 * Deserializes a RunConfig
 * 
 * @author kgebhardt
 * 
 */
public class Loader {
	@SuppressWarnings("unchecked")
	private SingleElementHandlerFactory<Observer<RunConfig>> getRootHandler(
			SingleElementHandlerFactory<RunConfigBuilder> prioritiesHandler) {
		return new SimpleElementHandlerFactory<Observer<RunConfig>, RunConfigBuilder>("runconfig",
				new CompositeElementHandlerFactory<RunConfigBuilder>(prioritiesHandler),
				RunConfigBuilder.createFactory(), new ComplexFieldProcessor<Observer<RunConfig>, RunConfigBuilder>() {

					@Override
					public void process(Observer<RunConfig> b1, RunConfigBuilder b2) {
						RunConfig rc = b2.build();
						b1.notify(rc);
					}
				}, RunConfigBuilder.createProcessor(), null);
	}

	private ParserImpl<RunConfig> createParser(Observer<RunConfig> o) {
		ParserImpl<RunConfig> p = new ParserImpl<RunConfig>(o);
		p.setRootState(new SimpleRootHandler<RunConfig>(p, getRootHandler(getPrioritiesHandler(getPriorityHandler()))));
		return p;
	}

	private SingleElementHandlerFactory<RunConfigBuilder> getPrioritiesHandler(
			SingleElementHandlerFactory<PrioritiesBuilder> priorityHandler) {
		return new SimpleElementHandlerFactory<RunConfigBuilder, PrioritiesBuilder>("priorities", priorityHandler,
				PrioritiesBuilder.createFactory(), new ComplexFieldProcessor<RunConfigBuilder, PrioritiesBuilder>() {
					@Override
					public void process(RunConfigBuilder b1, PrioritiesBuilder b2) {
						b1.priorities = b2.priorities;
					}
				}, null, null);
	}

	private SingleElementHandlerFactory<PrioritiesBuilder> getPriorityHandler() {
		return new SimpleElementHandlerFactory<PrioritiesBuilder, PriorityBuilder>("job", null,
				PriorityBuilder.createFactory(), new ComplexFieldProcessor<PrioritiesBuilder, PriorityBuilder>() {
					@Override
					public void process(PrioritiesBuilder b1, PriorityBuilder b2) {
						b1.priorities.put(b2.id, b2.priority);
					}
				}, PriorityBuilder.createProcessor(), null);
	}

	public RunConfig load(String path) throws Exception {
		RunConfigObserver rco = new RunConfigObserver();
		ParserImpl<RunConfig> p = createParser(rco);
		try {
			p.init(new File(path));
			p.process();
		} finally {
			p.done();
		}
		return rco.getRunConfig();
	}

	private static class RunConfigObserver implements Observer<RunConfig> {
		RunConfig rc;

		public RunConfig getRunConfig() {
			return rc;
		}

		@Override
		public void notify(RunConfig event) {
			rc = event;
		}

	}
}
