package org.vanda.datasources;

import javax.swing.JComponent;

import org.vanda.types.Type;
import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;

public abstract class DataSource {

	private String name;
	protected MultiplexObserver<DataSource> observers;

	public DataSource(String name) {
		observers = new MultiplexObserver<DataSource>();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Observable<DataSource> getObservable() {
		return observers;
	}

	abstract public JComponent getElementSelector();

	abstract public String getSelectedElement();

	abstract public void setSelectedElement(String element);

	abstract public String getValue(String element);

	abstract public Type getType(String element);

}
