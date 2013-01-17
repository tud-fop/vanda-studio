package org.vanda.workflows.toolinterfaces;

import java.io.File;

import org.vanda.util.Loader;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.Tool;
import org.vanda.xml.ParserImpl;

public class ToolLoader implements Loader<Tool> {

	private final String path;

	public ToolLoader(String file) {
		this.path = file;
	}

	private static ParserImpl<Tool> createParser(
			Observer<Tool> o) {
		ParserImpl<Tool> p = new ParserImpl<Tool>(o);
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
	
	public void loadFile(Observer<Tool> o, File f) {
		System.out.println("Processing: " + f.getAbsolutePath());
		ParserImpl<Tool> p = createParser(o);
		try {
			p.init(f);
			p.process();
		} catch (Exception e) {
			System.err.println("Tool interface file " + path
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
	public void load(Observer<Tool> o) {
		for (File f : (new File(path)).listFiles())
			if (f.isFile() && f.getAbsolutePath().endsWith(".xml"))
				loadFile(o, f);
	}

}
