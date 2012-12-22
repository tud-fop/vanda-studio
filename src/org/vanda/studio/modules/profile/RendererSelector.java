package org.vanda.studio.modules.profile;

import org.vanda.studio.model.elements.RendererAssortment;

public interface RendererSelector {
	String getIdentifier();
	
	<R> R selectRenderer(RendererAssortment<R> ra);
}
