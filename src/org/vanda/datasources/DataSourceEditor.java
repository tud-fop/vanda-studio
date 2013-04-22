package org.vanda.datasources;

import javax.swing.JComponent;

public interface DataSourceEditor {
	JComponent getComponent();
	DataSource getDataSource();
	void writeChange();
}
