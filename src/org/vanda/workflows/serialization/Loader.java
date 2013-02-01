package org.vanda.workflows.serialization;

import java.io.File;

import org.vanda.types.Types;
import org.vanda.util.Observer;
import org.vanda.util.Repository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.LiteralAdapter;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.ToolAdapter;
import org.vanda.xml.ComplexFieldProcessor;
import org.vanda.xml.CompositeElementHandlerFactory;
import org.vanda.xml.CompositeFieldProcessor;
import org.vanda.xml.Factory;
import org.vanda.xml.ParserImpl;
import org.vanda.xml.SimpleElementHandlerFactory;
import org.vanda.xml.SimpleRootHandler;
import org.vanda.xml.SimpleFieldProcessor;

public class Loader {
	private final Repository<Tool> tr;

	public Loader(Repository<Tool> tr) {
		this.tr = tr;
	}

	@SuppressWarnings("unchecked")
	private ParserImpl<MutableWorkflow> createParser(Observer<MutableWorkflow> o) {
		ParserImpl<MutableWorkflow> p = new ParserImpl<MutableWorkflow>(o);
		p.setRootState(new SimpleRootHandler<MutableWorkflow>(p,
				new SimpleElementHandlerFactory<Observer<MutableWorkflow>, WorkflowBuilder>("workflow",
						new SimpleElementHandlerFactory<WorkflowBuilder, JobBuilder>("job",
								new CompositeElementHandlerFactory<JobBuilder>(
										new SimpleElementHandlerFactory<JobBuilder, LiteralBuilder>("literal", null,
												new Factory<LiteralBuilder>() {
													@Override
													public LiteralBuilder create() {
														return new LiteralBuilder();
													}

												}, new ComplexFieldProcessor<JobBuilder, LiteralBuilder>() {
													@Override
													public void process(JobBuilder b1, LiteralBuilder b2) {
														b1.element = new LiteralAdapter(b2.build());
													}
												}, new CompositeFieldProcessor<LiteralBuilder>(
														new SimpleFieldProcessor<LiteralBuilder>("type") {
															@Override
															public void process(String name, String value,
																	LiteralBuilder b) {
																b.type = Types.parseType(null, null, value);
															}
														}, new SimpleFieldProcessor<LiteralBuilder>("value") {
															@Override
															public void process(String name, String value,
																	LiteralBuilder b) {
																b.value = value;
															}
														})),
										new SimpleElementHandlerFactory<JobBuilder, ToolBuilder>("tool", null,
												new Factory<ToolBuilder>() {
													@Override
													public ToolBuilder create() {
														return new ToolBuilder();
													}

												}, new ComplexFieldProcessor<JobBuilder, ToolBuilder>() {
													@Override
													public void process(JobBuilder b1, ToolBuilder b2) {
														b1.element = new ToolAdapter(b2.build(tr));
													}
												}, new CompositeFieldProcessor<ToolBuilder>(
														new SimpleFieldProcessor<ToolBuilder>("id") {
															@Override
															public void process(String name, String value, ToolBuilder b) {
																b.id = value;
															}
														})),
										new SimpleElementHandlerFactory<JobBuilder, BindingBuilder>("bind", null,
												new Factory<BindingBuilder>() {
													@Override
													public BindingBuilder create() {
														return new BindingBuilder();
													}

												}, new ComplexFieldProcessor<JobBuilder, BindingBuilder>() {
													@Override
													public void process(JobBuilder b1, BindingBuilder b2) {
														b1.bindings.put(b2.port, b2.variable);
													}
												}, new CompositeFieldProcessor<BindingBuilder>(
														new SimpleFieldProcessor<BindingBuilder>("port") {
															@Override
															public void process(String name, String value,
																	BindingBuilder b) {
																b.port = value;
															}
														}, new SimpleFieldProcessor<BindingBuilder>("variable") {
															@Override
															public void process(String name, String value,
																	BindingBuilder b) {
																b.variable = value;
															}
														})),
										new SimpleElementHandlerFactory<JobBuilder, GeometryBuilder>("geometry", null,
												new Factory<GeometryBuilder>() {
													@Override
													public GeometryBuilder create() {
														return new GeometryBuilder();
													}

												}, new ComplexFieldProcessor<JobBuilder, GeometryBuilder>() {
													@Override
													public void process(JobBuilder b1, GeometryBuilder b2) {
														b1.dimensions = b2.build();
													}
												}, new CompositeFieldProcessor<GeometryBuilder>(
														new SimpleFieldProcessor<GeometryBuilder>("x") {
															@Override
															public void process(String name, String value,
																	GeometryBuilder b) {
																b.x = Double.parseDouble(value);
															}
														}, new SimpleFieldProcessor<GeometryBuilder>("y") {
															@Override
															public void process(String name, String value,
																	GeometryBuilder b) {
																b.y = Double.parseDouble(value);
															}
														}, new SimpleFieldProcessor<GeometryBuilder>("width") {
															@Override
															public void process(String name, String value,
																	GeometryBuilder b) {
																b.width = Double.parseDouble(value);
															}
														}, new SimpleFieldProcessor<GeometryBuilder>("height") {
															@Override
															public void process(String name, String value,
																	GeometryBuilder b) {
																b.height = Double.parseDouble(value);
															}
														}))), new Factory<JobBuilder>() {
									@Override
									public JobBuilder create() {
										return new JobBuilder();
									}
								}, new ComplexFieldProcessor<WorkflowBuilder, JobBuilder>() {
									@Override
									public void process(WorkflowBuilder b1, JobBuilder b2) {
										b1.jbs.add(b2.build());
									}
								}, null), new Factory<WorkflowBuilder>() {
							@Override
							public WorkflowBuilder create() {
								return new WorkflowBuilder();
							}
						}, new ComplexFieldProcessor<Observer<MutableWorkflow>, WorkflowBuilder>() {
							@Override
							public void process(Observer<MutableWorkflow> b1, WorkflowBuilder b2) {
								b1.notify(b2.build());
							}
						}, new CompositeFieldProcessor<WorkflowBuilder>(new SimpleFieldProcessor<WorkflowBuilder>(
								"name") {
							@Override
							public void process(String name, String value, WorkflowBuilder b) {
								b.name = value;
							}
						}))));
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
