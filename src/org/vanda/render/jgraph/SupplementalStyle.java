package org.vanda.render.jgraph;

import java.util.Map;

import com.mxgraph.model.mxCell;

public interface SupplementalStyle {
	public void addStyle(Map<String, Object> style);
	
	public boolean updateStyle(mxCell cell, boolean enable);
	
	public String getStyleName();
}