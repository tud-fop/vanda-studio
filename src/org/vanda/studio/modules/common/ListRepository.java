/**
 * 
 */
package org.vanda.studio.modules.common;

import java.util.Collection;
import java.util.HashMap;

import org.vanda.util.MultiplexObserver;
import org.vanda.util.Observable;
import org.vanda.util.Observer;
import org.vanda.util.Repository;
import org.vanda.workflows.elements.RepositoryItem;

/**
 * @author buechse
 *
 */
public class ListRepository<T extends RepositoryItem>
		implements Repository<T> {
	MultiplexObserver<T> addObservable;
	MultiplexObserver<T> removeObservable;
	MultiplexObserver<T> modifyObservable;
	HashMap<String, T> items;

	/**
	 * @param l
	 *            A <code>Loader</code>, may be <code>null</code>
	 */
	public ListRepository() {
		addObservable = new MultiplexObserver<T>();
		modifyObservable = new MultiplexObserver<T>();
		removeObservable = new MultiplexObserver<T>();
		items = new HashMap<String, T>();
	}

	public void addItem(T newitem) {
		T item = items.remove(newitem.getId());
		if (item != newitem)
			removeObservable.notify(item);
		items.put(newitem.getId(), newitem);
		if (item != newitem)
			addObservable.notify(newitem);
	}

	@Override
	public boolean containsItem(String id) {
		return items.containsKey(id);
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
		return items.get(id);
	}

	@Override
	public Collection<T> getItems() {
		return items.values();
	}

	public Observer<T> getModifyObserver() {
		return modifyObservable;
	}

	@Override
	public void refresh() {
	}

}
