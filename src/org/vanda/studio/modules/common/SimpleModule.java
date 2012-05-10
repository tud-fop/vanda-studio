package org.vanda.studio.modules.common;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.model.elements.Tool;

public interface SimpleModule<V, T extends Tool<V>> extends Module {
	
	Editor<V, T> createEditor(Application app);
	
	ToolFactory<V, T> createFactory();
	
	ModuleInstance<V, T> createInstance(Application app);
	
	String getExtension();
	
}
