package org.vanda.workflows.toolinterfaces;

import java.io.File;

import org.vanda.util.Loader;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.Tool;
import org.vanda.xml.ComplexFieldProcessor;
import org.vanda.xml.CompositeElementHandlerFactory;
import org.vanda.xml.ParserImpl;
import org.vanda.xml.RootRootHandler;
import org.vanda.xml.SimpleElementHandlerFactory;
import org.vanda.xml.SimpleRootHandler;
import org.vanda.xml.SingleElementHandlerFactory;
import org.vanda.xml.StringBuilderFactory;
import org.vanda.xml.TextFieldProcessor;

public class ToolLoader implements Loader<Tool> {

	private final String path;

	public ToolLoader(String file) {
		this.path = file;
	}

	private static SingleElementHandlerFactory<ToolBuilder> inPortHandler() {
		return new SimpleElementHandlerFactory<ToolBuilder, PortBuilder>("in", null, PortBuilder.createFactory(),
				new ComplexFieldProcessor<ToolBuilder, PortBuilder>() {
					@Override
					public void process(ToolBuilder b1, PortBuilder b2) {
						b1.inPorts.add(b2.build(b1.ts, b1.tVars));
					}
				}, PortBuilder.createProcessor(), null);
	}

	private static SingleElementHandlerFactory<ToolBuilder> outPortHandler() {
		return new SimpleElementHandlerFactory<ToolBuilder, PortBuilder>("out", null, PortBuilder.createFactory(),
				new ComplexFieldProcessor<ToolBuilder, PortBuilder>() {
					@Override
					public void process(ToolBuilder b1, PortBuilder b2) {
						b1.outPorts.add(b2.build(b1.ts, b1.tVars));
					}
				}, PortBuilder.createProcessor(), null);
	}

	private static SingleElementHandlerFactory<RepositoryItemBuilder> descHandler() {
		return new SimpleElementHandlerFactory<RepositoryItemBuilder, StringBuilder>("description", null,
				new StringBuilderFactory(), new ComplexFieldProcessor<RepositoryItemBuilder, StringBuilder>() {
					@Override
					public void process(RepositoryItemBuilder b1, StringBuilder b2) {
						b1.description.append(b2);
					}
				}, null, new TextFieldProcessor());
	}

	@SuppressWarnings("unchecked")
	private static SingleElementHandlerFactory<ToolInterfaceBuilder> toolHandler(
			SingleElementHandlerFactory<ToolBuilder> inPortHandler,
			SingleElementHandlerFactory<ToolBuilder> outPortHandler,
			SingleElementHandlerFactory<RepositoryItemBuilder> descHandler) {
		return new SimpleElementHandlerFactory<ToolInterfaceBuilder, ToolBuilder>("tool",
				new CompositeElementHandlerFactory<ToolBuilder>(inPortHandler, outPortHandler, descHandler),
				ToolBuilder.createFactory(), new ComplexFieldProcessor<ToolInterfaceBuilder, ToolBuilder>() {
					@Override
					public void process(ToolInterfaceBuilder b1, ToolBuilder b2) {
						if ("".equals(b2.status))
							b1.tools.add(b2);
					}
				}, ToolBuilder.createProcessor(), null);
	}

	@SuppressWarnings("unchecked")
	private static SingleElementHandlerFactory<Observer<Tool>> toolInterfaceHandler(
			SingleElementHandlerFactory<ToolInterfaceBuilder> toolHandler,
			SingleElementHandlerFactory<RepositoryItemBuilder> descHandler) {
		return new SimpleElementHandlerFactory<Observer<Tool>, ToolInterfaceBuilder>("toolinterface",
				new CompositeElementHandlerFactory<ToolInterfaceBuilder>(toolHandler, descHandler),
				ToolInterfaceBuilder.createFactory(),
				new ComplexFieldProcessor<Observer<Tool>, ToolInterfaceBuilder>() {
					@Override
					public void process(Observer<Tool> b1, ToolInterfaceBuilder b2) {
						// TODO sanity checks
						for (StaticTool t : b2.build())
							b1.notify(t);
					}
				}, ToolInterfaceBuilder.createProcessor(), null);
	}

	private static ParserImpl<Tool> createParser(Observer<Tool> o) {
		ParserImpl<Tool> p = new ParserImpl<Tool>(o);
		// do the whole dependency injection thing
		SingleElementHandlerFactory<RepositoryItemBuilder> dhf = descHandler();
		p.setRootState(new RootRootHandler(p, "root", new SimpleRootHandler<Tool>(p, toolInterfaceHandler(
				toolHandler(inPortHandler(), outPortHandler(), dhf), dhf))));
		return p;
	}

	public void loadFile(Observer<Tool> o, File f) {
		System.out.println("Processing: " + f.getAbsolutePath());
		ParserImpl<Tool> p = createParser(o);
		try {
			p.init(f);
			p.process();
		} catch (Exception e) {
			System.err.println("Tool interface file " + f.getPath() + " can not be loaded.");
			e.printStackTrace();
			// app.sendMessage(new ExceptionMessage(e));
			// new ExceptionMessage(new Exception("Tool file "
			// + file.getAbsolutePath() + " can not be loaded.")));
		} finally {
			p.done();
		}
	}

	@Override
	public void load(Observer<Tool> o) {
		for (File f : (new File(path)).listFiles())
			if (f.isFile() && f.getAbsolutePath().toLowerCase().endsWith(".xml"))
				loadFile(o, f);
	}

}
