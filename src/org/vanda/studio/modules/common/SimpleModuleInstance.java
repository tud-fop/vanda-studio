package org.vanda.studio.modules.common;

import java.io.File;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.util.Observer;

public class SimpleModuleInstance<V, T extends Tool<V>>
		implements ModuleInstance<V, T> {

	protected Application app;
	protected Editor<V, T> editor;
	protected ToolFactory<V, T> factory;
	protected SimpleLoader<V, T> loader;
	protected SimpleModule<V, T> module;
	protected SimpleRepository<V, T> repository;

	public SimpleModuleInstance(Application a, SimpleModule<V, T> m) {
		app = a;
		module = m;

		factory = module.createFactory();
		if (factory != null) {
			loader = new SimpleLoader<V, T>(this,
					SimpleLoader.createExtensionFilter(module.getExtension()),
					factory);
		}
		repository = new SimpleRepository<V, T>(loader);
		app.getToolMetaRepository().addRepository(repository);
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
