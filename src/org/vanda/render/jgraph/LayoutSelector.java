package org.vanda.render.jgraph;

public interface LayoutSelector {
	<L> L selectLayout(LayoutAssortment<L> la);
}
