/**
 * 
 */
package org.vanda.studio.util;

import java.util.HashSet;

/**
 * Common base class for event sources (Observable).
 * <p>
 * Implements the Vanda Composer event distribution infrastructure.
 * 
 * @author rmueller
 * 
 */
public class MultiplexObserver<T> implements Observable<T>, Observer<T> {

	protected HashSet<Observer<T>> observers;

	public MultiplexObserver() {
		observers = new HashSet<Observer<T>>();
	}

	/**
	 */
	@Override
	public void addObserver(Observer<T> o) {
		// fail-fast behavior
		if (o == null)
			throw new IllegalArgumentException("observer must not be null");
		if (!observers.add(o))
			throw new UnsupportedOperationException("cannot add observer twice");
	}
	
	/**
	 */
	@Override
	public void removeObserver(Observer<T> o) {
		// fail-fast behavior
		if (o == null)
			throw new IllegalArgumentException("observer must not be null");
		if (!observers.remove(o))
			throw new UnsupportedOperationException("attempt to remove unregistereed observer");
	}

	/**
	 */
	@Override
	public void notify(T event) {
		for (Observer<T> o : observers)
			o.notify(event);
	}

}
