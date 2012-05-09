package org.vanda.studio.modules.common;

import org.vanda.studio.model.workflows.Tool;

public interface Editor<V, T extends Tool<V>> {
	void open(T o);
}
