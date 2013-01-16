package org.vanda.studio.modules.tools;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.util.ExternalRepository;
import org.vanda.workflows.elements.ToolInterface;
import org.vanda.workflows.toolinterfaces.ToolInterfaceLoader;

public class ToolsModule implements Module {

	@Override
	public String getName() {
		return "Tool interfaces module for Vanda Studio";
	}

	@Override
	public Object createInstance(Application a) {
		ExternalRepository<ToolInterface> er = new ExternalRepository<ToolInterface>(
				new ToolInterfaceLoader(a.getProperty("toolsPath") + "test.xml"));
		er.refresh();
		a.getToolInterfaceMetaRepository().addRepository(er);
		return er;
	}

}
