package org.vanda.studio.modules.profile.model;

import java.io.File;
import java.io.IOException;

public interface FragmentIO {
	
	// String makeUnique(String prefix, Object key);
	
	File createFile(String name) throws IOException;
	
}
