package org.vanda.workflows.toolinterfaces;

import org.vanda.workflows.elements.ToolInterface;

public interface Parser {
	void notify(ToolInterface ti);
	RuntimeException fail(Throwable e);
}
