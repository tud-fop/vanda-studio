package org.vanda.workflows.serialization;

import java.io.File;

import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.util.Repository;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;
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

	private SingleElementHandlerFactory<RowBuilder> assignmentHandler() {
		return new SimpleElementHandlerFactory<RowBuilder, AssignmentBuilder>("assignment", null,
				AssignmentBuilder.createFactory(), new ComplexFieldProcessor<RowBuilder, AssignmentBuilder>() {
					@Override
					public void process(RowBuilder b1, AssignmentBuilder b2) {
						b1.assignment.put(b2.key, b2.value);
					}
				}, AssignmentBuilder.createProcessor(), null);
	}

	private SingleElementHandlerFactory<DatabaseBuilder> rowHandler(
			SingleElementHandlerFactory<RowBuilder> assignmentHandler) {
		return new SimpleElementHandlerFactory<DatabaseBuilder, RowBuilder>("row", assignmentHandler,
				RowBuilder.createFactory(), new ComplexFieldProcessor<DatabaseBuilder, RowBuilder>() {
					@Override
					public void process(DatabaseBuilder b1, RowBuilder b2) {
						b1.assignments.add(b2.assignment);
					}
				}, null, null);
	}

	private SingleElementHandlerFactory<WorkflowBuilder> databaseHandler(
			SingleElementHandlerFactory<DatabaseBuilder> rowHandler) {
		return new SimpleElementHandlerFactory<WorkflowBuilder, DatabaseBuilder>("database", rowHandler,
				DatabaseBuilder.createFactory(), new ComplexFieldProcessor<WorkflowBuilder, DatabaseBuilder>() {
					@Override
					public void process(WorkflowBuilder b1, DatabaseBuilder b2) {
						b1.database = b2.build();
					}
				}, null, null);
	}

	private SingleElementHandlerFactory<Observer<Pair<MutableWorkflow, Database>>> workflowHandler(
			SingleElementHandlerFactory<WorkflowBuilder> jobHandler,
			SingleElementHandlerFactory<WorkflowBuilder> databaseHandler) {
		return new SimpleElementHandlerFactory<Observer<Pair<MutableWorkflow, Database>>, WorkflowBuilder>("workflow", jobHandler,
				WorkflowBuilder.createFactory(),
				new ComplexFieldProcessor<Observer<Pair<MutableWorkflow, Database>>, WorkflowBuilder>() {
					@Override
					public void process(Observer<Pair<MutableWorkflow, Database>> b1, WorkflowBuilder b2) {
						MutableWorkflow w = b2.build();
						Database d = b2.database;
						if (d == null) {
							final Database d2 = new Database();
							for (final Job j : w.getChildren()) {
								j.visit(new ElementVisitor() {
									@Override
									public void visitLiteral(Literal l) {
										d2.put(j.bindings.get(j.getOutputPorts().get(0)), l.getValue());
									}

									@Override
									public void visitTool(Tool t) {
									}
								});
							}
							d = d2;
						}
						b1.notify(new Pair<MutableWorkflow, Database>(w, d));
					}
				}, WorkflowBuilder.createProcessor(), null);
	}

	private ParserImpl<Pair<MutableWorkflow, Database>> createParser(Observer<Pair<MutableWorkflow, Database>> o) {
		ParserImpl<Pair<MutableWorkflow, Database>> p = new ParserImpl<Pair<MutableWorkflow, Database>>(o);
		p.setRootState(new SimpleRootHandler<Pair<MutableWorkflow, Database>>(p, workflowHandler(jobHandler(
				literalHandler(), toolHandler(), bindHandler(), geometryHandler()), databaseHandler(rowHandler(assignmentHandler())))));
		return p;
	}

	public Pair<MutableWorkflow, Database> load(String filename) throws Exception {
		WorkflowObserver o = new WorkflowObserver();
		ParserImpl<Pair<MutableWorkflow, Database>> p = createParser(o);
		try {
			p.init(new File(filename));
			p.process();
		} finally {
			p.done();
		}
		return o.getWorkflow();
	}

	private static final class WorkflowObserver implements Observer<Pair<MutableWorkflow, Database>> {
		private Pair<MutableWorkflow, Database> p;

		public Pair<MutableWorkflow, Database> getWorkflow() {
			return p;
		}

		@Override
		public void notify(Pair<MutableWorkflow, Database> event) {
			p = event;
		}
	}
}