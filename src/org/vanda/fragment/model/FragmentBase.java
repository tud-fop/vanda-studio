package org.vanda.fragment.model;

import org.vanda.types.Type;
import org.vanda.workflows.elements.Tool;

public interface FragmentBase {
	
	public Tool getConversionTool(Type from, Type to);
	
	public Fragment getFragment(String name);
	
}
