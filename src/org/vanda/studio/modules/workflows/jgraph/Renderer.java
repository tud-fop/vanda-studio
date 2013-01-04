package org.vanda.studio.modules.workflows.jgraph;

import java.util.Map;

import org.vanda.workflows.hyper.Job;

import com.mxgraph.model.mxCell;

public interface Renderer {
	void addStyle(Map<String, Object> style);

	String getStyleName();

	mxCell render(Job to, Graph g, Object parentCell);
}