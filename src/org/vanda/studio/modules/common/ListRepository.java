/**
 * 
 */
package org.vanda.studio.modules.common;

import java.util.ArrayList;
import java.util.Collection;

import org.vanda.studio.app.Repository;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;

/**
 * @author buechse
 *
 */
public class ListRepository<T>
		implements Repository<T> {
	MultiplexObserver<T> addObservable;
	MultiplexObserver<T> removeObservable;
	MultiplexObserver<T> modifyObservable;
	ArrayList<T> items;

	/**
	 * @param l
	 *            A <code>Loader</code>, may be <code>null</code>
	 */
	public ListRepository() {
		addObservable = new MultiplexObserver<T>();
		modifyObservable = new MultiplexObserver<T>();
		removeObservable = new MultiplexObserver<T>();
		items = new ArrayList<T>();
	}

	public void addItem(T newitem) {
		items.add(newitem);
		addObservable.notify(newitem);
	}

	@Override
	public boolean containsItem(String id) {
		return false;
	}

	@Override
	public Observable<T> getAddObservable() {
		return addObservable;
	}

	@Override
	public Observable<T> getRemoveObservable() {
		return removeObservable;
	}

	@Override
	public Observable<T> getModifyObservable() {
		return modifyObservable;
	}

	@Override
	public T getItem(String id) {
		return null;
	}

	@Override
	public Collection<T> getItems() {
		return items;
	}

	public Observer<T> getModifyObserver() {
		return modifyObservable;
	}

	@Override
	public void refresh() {
	}

}
