package org.vanda.datasources;

import org.vanda.studio.app.Application;
import org.vanda.types.Type;

public interface DataSource {

	ElementSelector createSelector();
	DataSourceEditor createEditor(Application app);
	String getValue(Element element);
	Type getType(Element element);

}
