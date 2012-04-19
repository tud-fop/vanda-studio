package org.vanda.studio.modules.common;

import java.io.File;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Tool;
import org.vanda.studio.modules.common.SimpleLoader;
import org.vanda.studio.util.Observer;

public class SimpleModuleInstance<T extends Tool>
implements ModuleInstance<T> {
	
	protected Application app;
	protected Editor<T> editor;
	protected ToolFactory<T> factory;
	protected SimpleLoader<T> loader;
	protected SimpleModule<T> module;
	protected SimpleRepository<Tool> repository;
	
	public SimpleModuleInstance(Application a, SimpleModule<T> m) {
		app = a;
		module = m;
		
		factory = module.createFactory();
		if (factory != null) {
			loader = new SimpleLoader<T>(
				this,
				SimpleLoader.createExtensionFilter(module.getExtension()),
				factory);
		}
		repository = new SimpleRepository<Tool>(loader);
		app.addRepository(repository);
		editor = module.createEditor(app);
	}
	
	@Override
	public void openEditor(T o) {
		if (editor != null)
			editor.open(o);
	}
	
	@Override
	public Observer<Tool> getModifyObserver() {
		return repository.getModifyObserver();
	}
	
	@Override
	public String getPath() {
		return "modules" + File.separator
				+ module.getName().toLowerCase().replace('.', File.separatorChar);
	}
}
