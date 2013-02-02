package org.vanda.workflows.serialization;

import java.io.File;

import org.vanda.util.Observer;
import org.vanda.util.Repository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.LiteralAdapter;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.ToolAdapter;
import org.vanda.xml.ComplexFieldProcessor;
import org.vanda.xml.CompositeElementHandlerFactory;
import org.vanda.xml.ParserImpl;
import org.vanda.xml.SimpleElementHandlerFactory;
import org.vanda.xml.SimpleRootHandler;
import org.vanda.xml.SingleElementHandlerFactory;

public class Loader {
	private final Repository<Tool> tr;

	public Loader(Repository<Tool> tr) {
		this.tr = tr;
	}

	private SingleElementHandlerFactory<JobBuilder> literalHandler() {
		return new SimpleElementHandlerFactory<JobBuilder, LiteralBuilder>("literal", null,
				LiteralBuilder.createFactory(), new ComplexFieldProcessor<JobBuilder, LiteralBuilder>() {
					@Override
					public void process(JobBuilder b1, LiteralBuilder b2) {
						b1.element = new LiteralAdapter(b2.build());
					}
				}, LiteralBuilder.createProcessor(), null);
	}

	private SingleElementHandlerFactory<JobBuilder> toolHandler() {
		return new SimpleElementHandlerFactory<JobBuilder, ToolBuilder>("tool", null, ToolBuilder.createFactory(),
				new ComplexFieldProcessor<JobBuilder, ToolBuilder>() {
					@Override
					public void process(JobBuilder b1, ToolBuilder b2) {
						b1.element = new ToolAdapter(b2.build(tr));
					}
				}, ToolBuilder.createProcessor(), null);
	}

	private SingleElementHandlerFactory<JobBuilder> bindHandler() {
		return new SimpleElementHandlerFactory<JobBuilder, BindingBuilder>("bind", null,
				BindingBuilder.createFactory(), new ComplexFieldProcessor<JobBuilder, BindingBuilder>() {
					@Override
					public void process(JobBuilder b1, BindingBuilder b2) {
						b1.bindings.put(b2.port, b2.variable);
					}
				}, BindingBuilder.createProcessor(), null);
	}

	private SingleElementHandlerFactory<JobBuilder> geometryHandler() {
		return new SimpleElementHandlerFactory<JobBuilder, GeometryBuilder>("geometry", null,
				GeometryBuilder.createFactory(), new ComplexFieldProcessor<JobBuilder, GeometryBuilder>() {
					@Override
					public void process(JobBuilder b1, GeometryBuilder b2) {
						b1.dimensions = b2.build();
					}
				}, GeometryBuilder.createProcessor(), null);
	}

	@SuppressWarnings("unchecked")
	private SingleElementHandlerFactory<WorkflowBuilder> jobHandler(
			SingleElementHandlerFactory<JobBuilder> literalHandler,
			SingleElementHandlerFactory<JobBuilder> toolHandler, SingleElementHandlerFactory<JobBuilder> bindHandler,
			SingleElementHandlerFactory<JobBuilder> geometryHandler) {
		return new SimpleElementHandlerFactory<WorkflowBuilder, JobBuilder>("job",
				new CompositeElementHandlerFactory<JobBuilder>(literalHandler, toolHandler, bindHandler,
						geometryHandler), JobBuilder.createFactory(),
				new ComplexFieldProcessor<WorkflowBuilder, JobBuilder>() {
					@Override
					public void process(WorkflowBuilder b1, JobBuilder b2) {
						b1.jbs.add(b2.build());
					}
				}, null, null);
	}

	private SingleElementHandlerFactory<Observer<MutableWorkflow>> workflowHandler(
			SingleElementHandlerFactory<WorkflowBuilder> jobHandler) {
		return new SimpleElementHandlerFactory<Observer<MutableWorkflow>, WorkflowBuilder>("workflow", jobHandler,
				WorkflowBuilder.createFactory(),
				new ComplexFieldProcessor<Observer<MutableWorkflow>, WorkflowBuilder>() {
					@Override
					public void process(Observer<MutableWorkflow> b1, WorkflowBuilder b2) {
						b1.notify(b2.build());
					}
				}, WorkflowBuilder.createProcessor(), null);
	}

	private ParserImpl<MutableWorkflow> createParser(Observer<MutableWorkflow> o) {
		ParserImpl<MutableWorkflow> p = new ParserImpl<MutableWorkflow>(o);
		p.setRootState(new SimpleRootHandler<MutableWorkflow>(p, workflowHandler(jobHandler(literalHandler(),
				toolHandler(), bindHandler(), geometryHandler()))));
		return p;
	}

	public MutableWorkflow load(String filename) throws Exception {
		WorkflowObserver o = new WorkflowObserver();
		ParserImpl<MutableWorkflow> p = createParser(o);
		try {
			p.init(new File(filename));
			p.process();
		} finally {
			p.done();
		}
		return o.getWorkflow();
	}

	private static final class WorkflowObserver implements Observer<MutableWorkflow> {
		private MutableWorkflow w;

		public MutableWorkflow getWorkflow() {
			return w;
		}

		@Override
		public void notify(MutableWorkflow event) {
			w = event;
		}
	}
}