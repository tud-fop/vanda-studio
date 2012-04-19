package org.vanda.studio.modules.common;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.model.Tool;

public interface SimpleModule<T extends Tool> extends Module {
	
	Editor<T> createEditor(Application app);
	
	ToolFactory<T> createFactory();
	
	ModuleInstance<T> createInstance(Application app);
	
	String getExtension();
	
}
