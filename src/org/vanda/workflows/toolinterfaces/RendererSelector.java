package org.vanda.workflows.toolinterfaces;

import org.vanda.workflows.elements.RendererAssortment;

public interface RendererSelector {
	String getIdentifier();
	
	<R> R selectRenderer(RendererAssortment<R> ra);
}
