package org.vanda.datasources;

import org.vanda.types.Type;

public interface DataSource {

	ElementSelector createSelector();
	String getValue(Element element);
	Type getType(Element element);

}
