/**
 * 
 */
package org.vanda.studio.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.app.Repository;
import org.vanda.studio.util.MultiplexObserver;
import org.vanda.studio.util.Observable;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.Util;

/**
 * @author buechse
 * 
 */
public final class CompositeRepository<T> implements Repository<T>, MetaRepository<T> {

	protected HashSet<Repository<? extends T>> repositories;

	protected MultiplexObserver<Repository<? extends T>> addRepositoryObservable;
	protected MultiplexObserver<Repository<? extends T>> removeRepositoryObservable;

	protected MultiplexObserver<T> addObservable;
	protected MultiplexObserver<T> modifyObservable;
	protected MultiplexObserver<T> removeObservable;

	public CompositeRepository() {
		repositories = new HashSet<Repository<? extends T>>();
		addRepositoryObservable = new MultiplexObserver<Repository<? extends T>>();
		removeRepositoryObservable = new MultiplexObserver<Repository<? extends T>>();
		addObservable = new MultiplexObserver<T>();
		modifyObservable = new MultiplexObserver<T>();
		removeObservable = new MultiplexObserver<T>();
		addRepositoryObservable
				.addObserver(new Observer<Repository<? extends T>>() {

					@Override
					public void notify(Repository<? extends T> r) {

						// "forward" child events
						r.getAddObservable().addObserver(addObservable);
						r.getRemoveObservable().addObserver(removeObservable);
						r.getModifyObservable().addObserver(modifyObservable);

						// pretend all items of r have been added
						Util.notifyAll(addObservable, r.getItems());

					}

				});

		removeRepositoryObservable
				.addObserver(new Observer<Repository<? extends T>>() {

					@Override
					public void notify(Repository<? extends T> r) {

						// stop forwarding
						r.getAddObservable().removeObserver(addObservable);
						r.getRemoveObservable()
								.removeObserver(removeObservable);
						r.getModifyObservable()
								.removeObserver(modifyObservable);

						// pretend all items of r have been removed
						Util.notifyAll(removeObservable, r.getItems());

					}

				});
	}

	@Override
	public Observable<Repository<? extends T>> getRepositoryAddObservable() {
		return addRepositoryObservable;
	}

	@Override
	public Observable<Repository<? extends T>> getRepositoryRemoveObservable() {
		return removeRepositoryObservable;
	}

	@Override
	public <T1 extends T> void addRepository(Repository<T1> r) {
		// fail-fast behavior
		if (r == null)
			throw new IllegalArgumentException("repository must not be null");
		if (!repositories.add(r))
			throw new UnsupportedOperationException(
					"cannot add repository twice");

		addRepositoryObservable.notify(r);
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

	@Override
	public <T1 extends T> void removeRepository(Repository<T1> r) {
		// fail-fast behavior
		if (r == null)
			throw new IllegalArgumentException("repository must not be null");
		if (!repositories.remove(r))
			throw new UnsupportedOperationException(
					"cannot add repository twice");

		removeRepositoryObservable.notify(r);

	}

	public Observable<T> getAddObservable() {
		return addObservable;
	}

	public Observable<T> getRemoveObservable() {
		return removeObservable;
	}

	public Observable<T> getModifyObservable() {
		return modifyObservable;
	}

}
