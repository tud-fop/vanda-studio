package org.vanda.studio.modules.common;

import org.vanda.studio.model.elements.Tool;

public interface Editor<T extends Tool> {
	void open(T o);
}
