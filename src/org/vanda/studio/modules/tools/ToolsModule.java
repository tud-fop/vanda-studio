package org.vanda.studio.modules.tools;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.util.ExternalRepository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.toolinterfaces.ToolLoader;

public class ToolsModule implements Module {

	@Override
	public String getName() {
		return "Tool interfaces module for Vanda Studio";
	}

	@Override
	public Object createInstance(Application a) {
		ExternalRepository<Tool> er = new ExternalRepository<Tool>(
				new ToolLoader(a.getProperty("toolsPath") + "test.xml"));
		er.refresh();
		a.getToolMetaRepository().addRepository(er);
		return er;
	}

}
