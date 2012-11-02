package org.vanda.studio.modules.workflows.jgraph;

import org.vanda.studio.model.Model;

import com.mxgraph.model.mxICell;
import com.mxgraph.view.mxGraph;

public interface Adapter {
	Adapter clone() throws CloneNotSupportedException;
	String getName();
	boolean inModel();
	void onInsert(mxGraph graph, mxICell parent, mxICell cell);
	void onRemove(mxICell parent);
	void onResize(mxGraph graph, mxICell parent, mxICell cell);
	void register(mxICell parent, mxICell cell);
	void setSelection(Model m);
}
