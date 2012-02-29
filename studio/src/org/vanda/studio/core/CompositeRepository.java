/**
 * 
 */
package org.vanda.studio.core;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;

import org.vanda.studio.model.Repository;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Util;

/**
 * @author buechse
 * 
 */
public class CompositeRepository<T> implements Repository<T> {

	protected HashSet<Repository<? extends T>> repositories;
	protected MultiplexObserver<Repository<T>> addRepositoryObservable;
	protected MultiplexObserver<Repository<T>> removeRepositoryObservable;
	protected MultiplexObserver<T> addObservable;
	protected MultiplexObserver<T> modifyObservable;
	protected MultiplexObserver<T> removeObservale;
	
	public CompositeRepository() {
		repositories = new HashSet<Repository<? extends T>>();
		addRepositoryObservable = new MultiplexObserver<Repository<T>>();
		removeRepositoryObservable = new MultiplexObserver<Repository<T>>();
		addObservable = new MultiplexObserver<T>();
		modifyObservable = new MultiplexObserver<T>();
		removeObservale = new MultiplexObserver<T>();
	}
		
	public Observable<Repository<T>> getRepositoryAddObservable() {
		return addRepositoryObservable;
	}
	
	public Observable<Repository<T>> getRepositoryRemoveObservable() {
		return removeRepositoryObservable;
	}
	
	public Observable<T> getAddObservable() {
		return addObservable;
	}

	public Observable<T> getRemoveObservable() {
		return removeObservale;
	}
	
	public Observable<T> getModifyObservable() {
		return modifyObservable;
	}

	/**
	 * Adds a {@link Repository} to the collection of repositories.
	 * 
	 * @param r
	 *            the Repository to add
	 */
	public void addRepository(final Repository<T> r) {
		// fail-fast behavior
		if (r == null)
			throw new IllegalArgumentException("repository must not be null");
		if (!repositories.add(r))
			throw new UnsupportedOperationException("cannot add repository twice");

		// "forward" child events
		r.getAddObservable().addObserver(addObservable);
		r.getRemoveObservable().addObserver(removeObservale);
		r.getModifyObservable().addObserver(modifyObservable);

		// pretend all items of r have been added
		Util.notifyAll(addObservable, r.getItems());

		addRepositoryObservable.notify(r);
	}
	
	/**
	 * Removes the given {@link VORepository} from the collection of
	 * repositories.
	 * 
	 * @param repository
	 *            the Repository that should get removed
	 */
	public void removeRepository(final Repository<T> r) {
		// fail-fast behavior
		if (r == null)
			throw new IllegalArgumentException("repository must not be null");
		if (!repositories.remove(r))
			throw new UnsupportedOperationException("cannot add repository twice");
		
		// stop forwarding
		r.getAddObservable().removeObserver(addObservable);
		r.getRemoveObservable().removeObserver(removeObservale);
		r.getModifyObservable().removeObserver(modifyObservable);
		
		// pretend all items of r have been removed
		Util.notifyAll(removeObservale, r.getItems());

		removeRepositoryObservable.notify(r);
	}

	public T getItem(String id) {
		for (Repository<? extends T> r : repositories) {
			T item = r.getItem(id);
			if (item != null)
				return item;
		}
		return null;
	}

	public boolean containsItem(String id) {
		for (Repository<? extends T> r : repositories) {
			if (r.containsItem(id))
				return true;
		}
		return false;
	}

	public Collection<T> getItems() {
		ArrayList<T> result = new ArrayList<T>();
		for (Repository<? extends T> r : repositories)
			result.addAll(r.getItems());
		return result;
	}
	
	public void refresh() {
		for (Repository<? extends T> r : repositories)
			r.refresh();
	}

}
