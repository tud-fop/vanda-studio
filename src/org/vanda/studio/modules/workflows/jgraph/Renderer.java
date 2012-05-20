package org.vanda.studio.modules.workflows.jgraph;

import java.util.Map;

import org.vanda.studio.model.hyper.Job;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public interface Renderer {
	void addStyle(Map<String, Object> style);

	String getStyleName();

	<F> mxCell render(Job<F> to, mxGraph g, Object parentCell);
}