package org.vanda.workflows.serialization;

import org.vanda.util.Repository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.LiteralAdapter;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.ToolAdapter;
import org.vanda.xml.CompositeElementHandlerFactory;
import org.vanda.xml.ElementHandler;
import org.vanda.xml.ElementHandlerFactory;
import org.vanda.xml.Parser;
import org.vanda.xml.SingleElementHandlerFactory;

public final class ElementHandlers {

	public interface WorkflowHandlerFactory {
		ElementHandler createHandler(Parser<MutableWorkflow> p);
	}

	public static SingleElementHandlerFactory<JobBuilder> createBindingHandlerFactory() {
		return new BindingHandlerFactoryImpl();
	}

	public static SingleElementHandlerFactory<JobBuilder> createGeometryHandlerFactory() {
		return new GeometryHandlerFactoryImpl();
	}

	public static SingleElementHandlerFactory<JobBuilder> createLiteralHandlerFactory() {
		return new LiteralHandlerFactoryImpl();
	}

	public static SingleElementHandlerFactory<JobBuilder> createToolHandlerFactory(
			Repository<Tool> tr) {
		return new ToolHandlerFactoryImpl(tr);
	}

	public static ElementHandlerFactory<WorkflowBuilder> createJobHandlerFactory(
			SingleElementHandlerFactory<JobBuilder> bhf,
			SingleElementHandlerFactory<JobBuilder> ghf,
			SingleElementHandlerFactory<JobBuilder>... eahf) {
		return new JobHandlerFactory(bhf, ghf, eahf);
	}

	public static WorkflowHandlerFactory createWorkflowHandlerFactory(
			ElementHandlerFactory<WorkflowBuilder> jhf) {
		return new WorkflowHandlerFactoryImpl(jhf);
	}

	public static ElementHandler createRootHandler(Parser<MutableWorkflow> p,
			WorkflowHandlerFactory wfh) {
		return new RootHandler(p, wfh);
	}

	private static class RootHandler implements ElementHandler {
		private final Parser<MutableWorkflow> p;
		private final WorkflowHandlerFactory wfHandler;

		public RootHandler(Parser<MutableWorkflow> p, WorkflowHandlerFactory tih) {
			this.p = p;
			wfHandler = tih;
		}

		@Override
		public void startElement(String namespace, String name) {
			// reset state (nothing to be done)
		}

		@Override
		public void handleAttribute(String namespace, String name, String value) {
			// does not happen
		}

		@Override
		public ElementHandler handleChild(String namespace, String name) {
			if ("workflow".equals(name))
				return wfHandler.createHandler(p);
			else
				throw p.fail(null);
		}

		@Override
		public void endElement(String namespace, String name) {
			// do nothing
		}

		@Override
		public void handleText(String text) {
			// ignore
		}
	}

	private static final class WorkflowHandlerFactoryImpl implements
			WorkflowHandlerFactory {

		private final ElementHandlerFactory<WorkflowBuilder> jhf;

		public WorkflowHandlerFactoryImpl(
				ElementHandlerFactory<WorkflowBuilder> jhf) {
			this.jhf = jhf;
		}

		@Override
		public ElementHandler createHandler(Parser<MutableWorkflow> p) {
			return new WorkflowHandler(p);
		}

		private final class WorkflowHandler implements ElementHandler {

			private final Parser<MutableWorkflow> p;
			private WorkflowBuilder wb;

			public WorkflowHandler(Parser<MutableWorkflow> p) {
				this.p = p;
			}

			@Override
			public void startElement(String namespace, String name) {
				wb = new WorkflowBuilder();
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				FieldProcessing.wfp.process(name, value, wb);
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				if ("job".equals(name))
					return jhf.create(name, p, wb);
				else
					throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
				p.getObserver().notify(wb.build());
			}

			@Override
			public void handleText(String text) {
			}

		}
	}

	private static final class JobHandlerFactory implements
			ElementHandlerFactory<WorkflowBuilder> {

		private final CompositeElementHandlerFactory<JobBuilder> eahf;

		@SuppressWarnings("unchecked")
		public JobHandlerFactory(SingleElementHandlerFactory<JobBuilder> bhf,
				SingleElementHandlerFactory<JobBuilder> ghf,
				SingleElementHandlerFactory<JobBuilder>... eahf) {
			this.eahf = new CompositeElementHandlerFactory<JobBuilder>();
			this.eahf.addHandlers(eahf);
			this.eahf.addHandler(bhf);
			this.eahf.addHandler(ghf);
		}

		@Override
		public ElementHandler create(String name, Parser<?> p,
				WorkflowBuilder wb) {
			return new JobHandler(p, wb);
		}

		private final class JobHandler implements ElementHandler {

			private final Parser<?> p;
			private final WorkflowBuilder wb;
			private JobBuilder jb;

			public JobHandler(Parser<?> p, WorkflowBuilder wb) {
				this.p = p;
				this.wb = wb;
			}

			@Override
			public void startElement(String namespace, String name) {
				jb = new JobBuilder();
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				// no attributes so far
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				ElementHandler result = eahf.create(name, p, jb);
				if (result != null)
					return result;
				else
					throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
				wb.jbs.add(jb.build());
			}

			@Override
			public void handleText(String text) {
			}
		}
	}

	private static final class LiteralHandlerFactoryImpl implements
			SingleElementHandlerFactory<JobBuilder> {

		@Override
		public ElementHandler create(String tag, Parser<?> p, JobBuilder jb) {
			return new LiteralHandler(p, jb);
		}

		@Override
		public String getTag() {
			return "literal";
		}

		private final class LiteralHandler implements ElementHandler {

			private final Parser<?> p;
			private final JobBuilder jb;
			private LiteralBuilder lb;

			public LiteralHandler(Parser<?> p, JobBuilder jb) {
				this.p = p;
				this.jb = jb;
			}

			@Override
			public void startElement(String namespace, String name) {
				lb = new LiteralBuilder();
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				FieldProcessing.lfp.process(name, value, lb);
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
				jb.element = new LiteralAdapter(lb.build());
			}

			@Override
			public void handleText(String text) {
			}
		}
	}

	private static final class ToolHandlerFactoryImpl implements
			SingleElementHandlerFactory<JobBuilder> {

		private final Repository<Tool> tr;

		public ToolHandlerFactoryImpl(Repository<Tool> tr) {
			this.tr = tr;
		}

		@Override
		public String getTag() {
			return "tool";
		}

		@Override
		public ElementHandler create(String tag, Parser<?> p, JobBuilder jb) {
			return new ToolHandler(p, jb);
		}

		private final class ToolHandler implements ElementHandler {

			private final Parser<?> p;
			private final JobBuilder jb;
			private ToolBuilder tb;

			public ToolHandler(Parser<?> p, JobBuilder jb) {
				this.p = p;
				this.jb = jb;
			}

			@Override
			public void startElement(String namespace, String name) {
				tb = new ToolBuilder();
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				FieldProcessing.tfp.process(name, value, tb);
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
				jb.element = new ToolAdapter(tb.build(tr));
			}

			@Override
			public void handleText(String text) {
			}
		}
	}

	private static final class BindingHandlerFactoryImpl implements
			SingleElementHandlerFactory<JobBuilder> {

		@Override
		public ElementHandler create(String tag, Parser<?> p, JobBuilder jb) {
			return new BindingHandler(p, jb);
		}

		@Override
		public String getTag() {
			return "bind";
		}

		private final class BindingHandler implements ElementHandler {

			private final Parser<?> p;
			private final JobBuilder jb;
			private BindingBuilder bb;

			public BindingHandler(Parser<?> p, JobBuilder jb) {
				this.p = p;
				this.jb = jb;
			}

			@Override
			public void startElement(String namespace, String name) {
				bb = new BindingBuilder();
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				FieldProcessing.bfp.process(name, value, bb);
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
				jb.bindings.put(bb.port, bb.variable);
			}

			@Override
			public void handleText(String text) {
			}
		}
	}

	private static final class GeometryHandlerFactoryImpl implements
			SingleElementHandlerFactory<JobBuilder> {

		@Override
		public ElementHandler create(String tag, Parser<?> p, JobBuilder jb) {
			return new GeometryHandler(p, jb);
		}

		@Override
		public String getTag() {
			return "geometry";
		}

		private final class GeometryHandler implements ElementHandler {

			private final Parser<?> p;
			private final JobBuilder jb;
			private GeometryBuilder gb;

			public GeometryHandler(Parser<?> p, JobBuilder jb) {
				this.p = p;
				this.jb = jb;
			}

			@Override
			public void startElement(String namespace, String name) {
				gb = new GeometryBuilder();
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				FieldProcessing.gfp.process(name, value, gb);
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
				jb.dimensions = gb.build();
			}

			@Override
			public void handleText(String text) {
			}
		}
	}
}