package org.vanda.fragment.bash;

import org.vanda.workflows.elements.RendererAssortment;

public interface RendererSelector {
	String getIdentifier();
	
	<R> R selectRenderer(RendererAssortment<R> ra);
}
