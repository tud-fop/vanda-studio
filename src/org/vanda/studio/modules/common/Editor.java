package org.vanda.studio.modules.common;

import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.model.workflows.ToolInstance;

public interface Editor<V, I extends ToolInstance, T extends Tool<V, I>> {
	void open(T o);
}
