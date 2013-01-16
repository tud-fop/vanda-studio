package org.vanda.workflows.toolinterfaces;

public class ElementHandlers {

	public interface DescriptionHandlerFactory {
		ElementHandler createHandler(Parser p, RepositoryItemBuilder tb);
	}

	public interface PortHandlerFactory {
		ElementHandler createPortHandler(Parser p, ToolBuilder tb);
	}

	public interface ToolHandlerFactory {
		ElementHandler createToolHandler(Parser p, ToolInterfaceBuilder tib);
	}

	public interface ToolInterfacesHandlerFactory {
		ElementHandler createToolInterfacesHandler(Parser p);
	}

	public static ElementHandler createRootHandler(Parser p,
			ToolInterfacesHandlerFactory tih) {
		return new RootHandler(p, tih);
	}

	public static ToolInterfacesHandlerFactory createToolInterfacesHandlerFactory(
			ToolHandlerFactory thf, DescriptionHandlerFactory dhf) {
		return new ToolInterfacesHandlerFactoryImpl(thf, dhf);
	}

	public static ToolHandlerFactory createToolHandlerFactory(
			PortHandlerFactory ihf, PortHandlerFactory ohf,
			DescriptionHandlerFactory dhf) {
		return new ToolHandlerFactoryImpl(ihf, ohf, dhf);
	}

	public static DescriptionHandlerFactory createDescriptionHandlerFactory() {
		return new DescriptionHandlerFactoryImpl();
	}

	public static PortHandlerFactory createInPortHandlerFactory() {
		return new InHandlerFactory();
	}

	public static PortHandlerFactory createOutPortHandlerFactory() {
		return new OutHandlerFactory();
	}

	private static class RootHandler implements ElementHandler {
		private final Parser p;
		private final ToolInterfacesHandlerFactory tiHandler;

		public RootHandler(Parser p, ToolInterfacesHandlerFactory tih) {
			this.p = p;
			tiHandler = tih;
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
			if ("root".equals(name))
				return this; // XXX a little hacky: it permits nested root
			if ("toolinterface".equals(name))
				return tiHandler.createToolInterfacesHandler(p);
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

	private static class ToolInterfacesHandlerFactoryImpl implements
			ToolInterfacesHandlerFactory {
		private final ToolHandlerFactory tHandler;
		private final DescriptionHandlerFactory dHandler;

		public ToolInterfacesHandlerFactoryImpl(ToolHandlerFactory th,
				DescriptionHandlerFactory dh) {
			tHandler = th;
			dHandler = dh;
		}

		@Override
		public ElementHandler createToolInterfacesHandler(Parser p) {
			return new ToolInterfacesHandler(p);
		}

		public class ToolInterfacesHandler implements ElementHandler {
			private final Parser p;
			private final ToolInterfaceBuilder tib;
			private boolean desc = false;

			public ToolInterfacesHandler(Parser p) {
				this.p = p;
				tib = new ToolInterfaceBuilder();
				desc = false;
			}

			@Override
			public void startElement(String namespace, String name) {
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				FieldProcessors.processRepositoryItemField(name, value, tib);
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				if (!desc && "description".equals(name)) {
					desc = true;
					return dHandler.createHandler(p, tib);
				} else if ("tool".equals(name)) {
					return tHandler.createToolHandler(p, tib);
				} else
					throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
				// TODO sanity checks
				p.notify(tib.build());
			}

			@Override
			public void handleText(String text) {
				// ignore
			}
		}
	}

	private static class ToolHandlerFactoryImpl implements ToolHandlerFactory {

		private final PortHandlerFactory inPortHandlerFactory;
		private final PortHandlerFactory outPortHandlerFactory;
		private final DescriptionHandlerFactory descriptionHandlerFactory;

		public ToolHandlerFactoryImpl(PortHandlerFactory inPortHandlerFactory,
				PortHandlerFactory outPortHandlerFactory,
				DescriptionHandlerFactory descriptionHandlerFactory) {
			this.inPortHandlerFactory = inPortHandlerFactory;
			this.outPortHandlerFactory = outPortHandlerFactory;
			this.descriptionHandlerFactory = descriptionHandlerFactory;
		}

		@Override
		public ElementHandler createToolHandler(Parser p,
				ToolInterfaceBuilder tib) {
			return new ToolHandler(p, tib);
		}

		public class ToolHandler implements ElementHandler {
			private boolean desc = false;
			private final Parser p;
			private final ToolInterfaceBuilder tib;
			private final ToolBuilder tb;

			public ToolHandler(Parser p, ToolInterfaceBuilder tib) {
				this.p = p;
				this.tib = tib;
				tb = new ToolBuilder();
			}

			@Override
			public void startElement(String namespace, String name) {
				desc = false;
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				FieldProcessors.processRepositoryItemField(name, value, tb);
				FieldProcessors.processToolField(name, value, tb);
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				if (!desc && "description".equals(name)) {
					desc = true;
					return descriptionHandlerFactory.createHandler(p, tb);
				} else if ("in".equals(name)) {
					return inPortHandlerFactory.createPortHandler(p, tb);
				} else if ("out".equals(name)) {
					return outPortHandlerFactory.createPortHandler(p, tb);
				} else
					throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
				// TODO sanity checks
				if ("".equals(tb.status))
					tib.tools.add(tb);
				// only add stable tools (status=="")
			}

			@Override
			public void handleText(String text) {
				// ignore
			}
		}
	}

	private static class InHandlerFactory implements PortHandlerFactory {

		@Override
		public ElementHandler createPortHandler(Parser p, ToolBuilder tb) {
			return new InHandler(p, tb);
		}

		public static class InHandler implements ElementHandler {
			private final Parser p;
			private final ToolBuilder tb;
			private final PortBuilder pb;

			public InHandler(Parser p, ToolBuilder tb) {
				this.p = p;
				this.tb = tb;
				pb = new PortBuilder();
			}

			@Override
			public void startElement(String namespace, String name) {
				pb.reset();
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				FieldProcessors.processPortField(name, value, tb, pb);
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
				tb.inPorts.add(pb.build());
			}

			@Override
			public void handleText(String text) {
				// ignore
			}
		}
	}

	private static class OutHandlerFactory implements PortHandlerFactory {

		@Override
		public ElementHandler createPortHandler(Parser p, ToolBuilder tb) {
			return new OutHandler(p, tb);
		}

		public static class OutHandler implements ElementHandler {
			private final Parser p;
			private final ToolBuilder tb;
			private final PortBuilder pb;

			public OutHandler(Parser p, ToolBuilder tb) {
				this.p = p;
				this.tb = tb;
				pb = new PortBuilder();
			}

			@Override
			public void startElement(String namespace, String name) {
				pb.reset();
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				FieldProcessors.processPortField(name, value, tb, pb);
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
				if ("".equals(pb.name) || pb.type == null)
					p.fail(null);
				tb.outPorts.add(pb.build());
			}

			@Override
			public void handleText(String text) {
				// ignore
			}
		}
	}

	private static class DescriptionHandlerFactoryImpl implements
			DescriptionHandlerFactory {

		@Override
		public ElementHandler createHandler(Parser p, RepositoryItemBuilder b) {
			return new DescriptionHandler(p, b);
		}

		public static class DescriptionHandler implements ElementHandler {
			private final Parser p;
			private final RepositoryItemBuilder b;

			public DescriptionHandler(Parser p, RepositoryItemBuilder b) {
				this.p = p;
				this.b = b;
			}

			@Override
			public void startElement(String namespace, String name) {
				b.description.setLength(0);
			}

			@Override
			public void handleAttribute(String namespace, String name,
					String value) {
				// ignore
			}

			@Override
			public ElementHandler handleChild(String namespace, String name) {
				throw p.fail(null);
			}

			@Override
			public void endElement(String namespace, String name) {
			}

			@Override
			public void handleText(String text) {
				b.description.append(text);
			}
		}
	}
}