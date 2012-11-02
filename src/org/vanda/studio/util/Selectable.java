package org.vanda.studio.util;

import org.vanda.studio.app.Application;

public interface Selectable {
	
	public void onSelect(Application app);
	
	public void onDeselect(Application app);
	
}
