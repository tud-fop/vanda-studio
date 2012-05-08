package org.vanda.studio.modules.common;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.model.workflows.ToolInstance;

public interface SimpleModule<V, I extends ToolInstance, T extends Tool<V, I>> extends Module {
	
	Editor<V, I, T> createEditor(Application app);
	
	ToolFactory<V, I, T> createFactory();
	
	ModuleInstance<V, I, T> createInstance(Application app);
	
	String getExtension();
	
}
