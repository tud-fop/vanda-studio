package org.vanda.render.jgraph;

import org.vanda.workflows.hyper.Job;

import com.mxgraph.view.mxStylesheet;

public interface LayoutManagerFactoryInterface {
	public LayoutManagerInterface getLayoutManager(Job job);
	public mxStylesheet getStylesheet();

}
