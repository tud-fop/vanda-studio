package org.vanda.render.jgraph;

public interface LayoutAssortment<L> {
	L getInport();
	L getOutport();
	L getLocation();
	L getJob();
	L getConnection();
	L getWorkflow();
}
