package org.vanda.workflows.toolinterfaces;

import java.io.File;

import org.vanda.util.Loader;
import org.vanda.util.Observer;
import org.vanda.xml.ParserImpl;

public class ToolLoader implements Loader<StaticTool> {

	private final String path;

	public ToolLoader(String file) {
		this.path = file;
	}

	private static ParserImpl<StaticTool> createParser(
			Observer<StaticTool> o) {
		ParserImpl<StaticTool> p = new ParserImpl<StaticTool>(o);
		// do the whole dependency injection thing
		ElementHandlers.DescriptionHandlerFactory dhf = ElementHandlers
				.createDescriptionHandlerFactory();
		p.setRootState(ElementHandlers.createRootHandler(p, ElementHandlers
				.createToolInterfacesHandlerFactory(ElementHandlers
						.createToolHandlerFactory(
								ElementHandlers.createInPortHandlerFactory(),
								ElementHandlers.createOutPortHandlerFactory(),
								dhf), dhf)));
		return p;
	}
	
	public void loadFile(Observer<StaticTool> o, File f) {
		System.out.println("Processing: " + f.getAbsolutePath());
		ParserImpl<StaticTool> p = createParser(o);
		try {
			p.init(f);
			p.process();
		} catch (Exception e) {
			System.err.println("Tool interface file " + f.getPath()
					+ " can not be loaded.");
			e.printStackTrace();
			// app.sendMessage(new ExceptionMessage(e));
			// new ExceptionMessage(new Exception("Tool file "
			// + file.getAbsolutePath() + " can not be loaded.")));
		} finally {
			p.done();
		}
	}

	@Override
	public void load(Observer<StaticTool> o) {
		for (File f : (new File(path)).listFiles())
			if (f.isFile() && f.getAbsolutePath().toLowerCase().endsWith(".xml"))
				loadFile(o, f);
	}

}
