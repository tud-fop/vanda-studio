package org.vanda.render.jgraph;

import java.util.Map;

public interface Renderer {
	public String getStyleName();

	public void addStyle(Map<String, Object> style);
	
	public void render(Cell container);
}