package org.vanda.studio.modules.common;

import org.vanda.studio.model.VObject;
import org.vanda.studio.util.Observer;

public interface Editor<T extends VObject> {
	void open(T o);
}
