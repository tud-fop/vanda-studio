package org.vanda.studio.modules.common;

import java.io.File;

import org.vanda.studio.model.elements.Tool;

public interface ToolFactory<V, T extends Tool<V>> {
	T createInstance(ModuleInstance<V, T> mod, File f);
}

