package org.vanda.studio.modules.common;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.model.VObject;

public interface SimpleModule<T extends VObject> extends Module {
	
	Editor<T> createEditor(Application app);
	
	VObjectFactory<T> createFactory();
	
	ModuleInstance<T> createInstance(Application app);
	
	String getExtension();
	
}
