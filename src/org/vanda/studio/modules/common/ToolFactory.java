package org.vanda.studio.modules.common;

import java.io.File;

import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.model.workflows.ToolInstance;

public interface ToolFactory<V, I extends ToolInstance, T extends Tool<V, I>> {
	T createInstance(ModuleInstance<V, I, T> mod, File f);
}

