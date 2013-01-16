package org.vanda.workflows.toolinterfaces;

import java.io.File;

import org.vanda.util.Loader;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.ToolInterface;
import org.vanda.workflows.toolinterfaces.ElementHandlers.DescriptionHandlerFactory;

public class ToolInterfaceLoader implements Loader<ToolInterface> {

	private final String file;

	public ToolInterfaceLoader(String file) {
		this.file = file;
	}

	@Override
	public void load(Observer<ToolInterface> o) {
		ParserImpl p = new ParserImpl(o);
		// do the whole dependency injection thing
		DescriptionHandlerFactory dhf = ElementHandlers
				.createDescriptionHandlerFactory();
		p.setRootState(ElementHandlers.createRootHandler(p, ElementHandlers
				.createToolInterfacesHandlerFactory(ElementHandlers
						.createToolHandlerFactory(
								ElementHandlers.createInPortHandlerFactory(),
								ElementHandlers.createOutPortHandlerFactory(),
								dhf), dhf)));
		try {
			p.init(new File(file));
			p.process();
		} catch (Exception e) {
			System.err.println("Tool interface file " + file
					+ " can not be loaded.");
			e.printStackTrace();
			// app.sendMessage(new ExceptionMessage(e));
			// new ExceptionMessage(new Exception("Tool file "
			// + file.getAbsolutePath() + " can not be loaded.")));
		} finally {
			p.done();
		}
	}

}
