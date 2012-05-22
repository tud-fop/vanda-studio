package org.vanda.studio.app;

import org.vanda.studio.model.elements.Ports;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.types.CompositeType;
import org.vanda.studio.model.types.Type;

public interface Profile extends RepositoryItem {
	Generator createGenerator();
	
	public static Type genericType = Ports.typeVariable;
	public static Type haskellType = new CompositeType("haskell");
	public static Type shellType = new CompositeType("shell");
}
