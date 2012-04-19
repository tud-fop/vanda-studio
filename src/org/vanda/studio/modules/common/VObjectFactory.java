package org.vanda.studio.modules.common;

import java.io.File;

import org.vanda.studio.model.Tool;

public interface VObjectFactory<T extends Tool> {
	T createInstance(ModuleInstance<T> mod, File f);
}

