package org.vanda.render.jgraph;

import java.util.Map;

public interface LayoutManagerInterface {
	public void register(Cell cell);
	public void setUpLayout(Graph g);
	public String getStyleName();
	public void addStyle(Map<String, Object> style);
}
