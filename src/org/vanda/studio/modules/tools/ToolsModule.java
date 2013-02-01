package org.vanda.studio.modules.tools;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.util.ExternalRepository;
import org.vanda.workflows.toolinterfaces.StaticTool;
import org.vanda.workflows.toolinterfaces.ToolLoader;

public class ToolsModule implements Module {

	@Override
	public String getName() {
		return "Tool interfaces module for Vanda Studio";
	}

	@Override
	public Object createInstance(Application a) {
		ExternalRepository<StaticTool> er = new ExternalRepository<StaticTool>(
				new ToolLoader(a.getProperty("toolsPath")));
		er.refresh();
		a.getToolMetaRepository().addRepository(er);
		return er;
	}

}
