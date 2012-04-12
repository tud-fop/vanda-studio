package org.vanda.studio.modules.common;

import org.vanda.studio.model.VObject;

public interface Editor<T extends VObject> {
	void open(T o);
}
