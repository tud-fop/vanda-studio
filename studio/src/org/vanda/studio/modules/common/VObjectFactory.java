package org.vanda.studio.modules.common;

import java.io.File;

import org.vanda.studio.model.VObject;

public interface VObjectFactory<T extends VObject> {
	T createInstance(ModuleInstance<T> mod, File f);
}

