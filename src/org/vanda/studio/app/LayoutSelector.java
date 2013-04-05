package org.vanda.studio.app;

public interface LayoutSelector {
	<L> L selectLayout(LayoutAssortment<L> la);
}
