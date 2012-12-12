/**
 * 
 */
package org.vanda.studio.modules.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.vanda.studio.app.Repository;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Util;

/**
 * @author buechse
 *
 */
public class ExternalRepository<T extends RepositoryItem>
		implements Repository<T> {
	MultiplexObserver<T> addObservable;
	MultiplexObserver<T> removeObservable;
	MultiplexObserver<T> modifyObservable;
	HashMap<String, T> items;
	Loader<T> loader;

	/**
	 * @param l
	 *            A <code>Loader</code>, may be <code>null</code>
	 */
	public ExternalRepository(Loader<T> l) {
		addObservable = new MultiplexObserver<T>();
		modifyObservable = new MultiplexObserver<T>();
		removeObservable = new MultiplexObserver<T>();
		items = new HashMap<String, T>();
		loader = l;
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
		if (loader != null) {
			RefreshHelper<T> r = new RefreshHelper<T>(items);
			loader.load(r);
			items = r.getNewItems();
			Util.notifyAll(addObservable, r.getAdds());
			Util.notifyAll(removeObservable, r.getRemoves());
		}
	}

	protected static class RefreshHelper<T extends RepositoryItem>
			implements Observer<T> {
		protected LinkedList<T> adds;
		protected LinkedList<T> removes;
		protected HashMap<String, T> items;
		protected HashMap<String, T> newitems;

		public RefreshHelper(HashMap<String, T> items) {
			adds = new LinkedList<T>();
			removes = new LinkedList<T>();
			this.items = items;
			newitems = new HashMap<String, T>();
		}

		@Override
		public void notify(T newitem) {
			T item = items.remove(newitem.getId());
			if (item != null) {
				if (item.getVersion().equals(newitem.getVersion()))
					newitem = item;
				//else
				//	removes.add(item);
			}
			newitems.put(newitem.getId(), newitem);
			if (item != newitem)
				adds.add(newitem);
		}

		public Collection<T> getAdds() {
			return adds;
		}

		public HashMap<String, T> getNewItems() {
			return newitems;
		}

		public Collection<T> getRemoves() {
			return removes;
		}
	}
}
