package org.vanda.studio.modules.profile.model;

import java.util.Collections;
import java.util.Set;

/**
 * Fragments represent small compositional snippets of code.
 * 
 * @author buechse
 *
 */
public class Fragment {
	public final String name;
	public final String text;
	public final Set<String> dependencies;
	public final Set<String> imports;
	
	public Fragment(String name) {
		this.name = name;
		this.text = "";
		this.dependencies = Collections.emptySet();
		this.imports = Collections.emptySet();
	}
	
	public Fragment(String name, String text, Set<String> dependencies, Set<String> imports) {
		this.name = name;
		this.text = text;
		this.dependencies = dependencies;
		this.imports = imports;
	}
	
	public static String normalize(String name) {
		return name.replace('$', '_').replace(' ', '_');
	}

}
