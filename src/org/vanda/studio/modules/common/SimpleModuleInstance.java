package org.vanda.studio.modules.common;

import java.io.File;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.model.workflows.ToolInstance;
import org.vanda.studio.modules.common.SimpleLoader;
import org.vanda.studio.util.Observer;

public class SimpleModuleInstance<V, I extends ToolInstance, T extends Tool<V, I>>
		implements ModuleInstance<V, I, T> {

	protected Application app;
	protected Editor<V, I, T> editor;
	protected ToolFactory<V, I, T> factory;
	protected SimpleLoader<V, I, T> loader;
	protected SimpleModule<V, I, T> module;
	protected SimpleRepository<T> repository;

	public SimpleModuleInstance(Application a, SimpleModule<V, I, T> m) {
		app = a;
		module = m;

		factory = module.createFactory();
		if (factory != null) {
			loader = new SimpleLoader<V, I, T>(this,
					SimpleLoader.createExtensionFilter(module.getExtension()),
					factory);
		}
		repository = new SimpleRepository<T>(loader);
		app.getToolRR().addRepository(repository);
		editor = module.createEditor(app);
	}

	@Override
	public void openEditor(T o) {
		if (editor != null)
			editor.open(o);
	}

	@Override
	public Observer<T> getModifyObserver() {
		return repository.getModifyObserver();
	}

	@Override
	public String getPath() {
		return "modules"
				+ File.separator
				+ module.getName().toLowerCase()
						.replace('.', File.separatorChar);
	}
}
