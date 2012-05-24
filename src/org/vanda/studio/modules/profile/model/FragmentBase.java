package org.vanda.studio.modules.profile.model;

import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.types.Type;

public interface FragmentBase {
	
	public Tool getConversionTool(Type from, Type to);
	
	public Fragment getFragment(String name);
	
}
