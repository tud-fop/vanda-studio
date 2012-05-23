package org.vanda.studio.modules.profile.model;

import java.io.File;

public interface FragmentIO {
	
	String makeUnique(String prefix);
	
	File createFile(String name);
	
}
