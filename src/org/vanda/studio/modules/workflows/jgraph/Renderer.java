package org.vanda.studio.modules.workflows.jgraph;

import java.util.Map;

import org.vanda.studio.model.hyper.HyperJob;

import com.mxgraph.view.mxGraph;

public interface Renderer {
	void addStyle(Map<String, Object> style);

	String getStyleName();

	void render(HyperJob<?> to, mxGraph g, Object parentCell);
}