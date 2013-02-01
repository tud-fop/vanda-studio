package org.vanda.workflows.serialization;

import java.io.File;

import org.vanda.util.Observer;
import org.vanda.util.Repository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.xml.ParserImpl;

public class Loader {
	private final Repository<Tool> tr;
	
	public Loader(Repository<Tool> tr) {
		this.tr = tr;
	}

	private ParserImpl<MutableWorkflow> createParser(Observer<MutableWorkflow> o) {
		ParserImpl<MutableWorkflow> p = new ParserImpl<MutableWorkflow>(o);
		// do the whole dependency injection thing
		p.setRootState(ElementHandlers.createRootHandler(p, ElementHandlers
				.createWorkflowHandlerFactory(ElementHandlers
						.createJobHandlerFactory(
								ElementHandlers.createBindingHandlerFactory(),
								ElementHandlers.createGeometryHandlerFactory(),
								ElementHandlers.createLiteralHandlerFactory(),
								ElementHandlers.createToolHandlerFactory(tr)))));
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
