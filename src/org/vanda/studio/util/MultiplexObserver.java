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
public class MultiplexObserver<T> implements Observable<T>, Observer<T>, Cloneable {

	protected HashSet<Observer<? super T>> observers;

	public MultiplexObserver() {
		observers = new HashSet<Observer<? super T>>();
	}

	/**
	 */
	@Override
	public void addObserver(Observer<? super T> o) {
		// fail-fast behavior
		if (o == null)
			throw new IllegalArgumentException("observer must not be null");
		if (!observers.add(o))
			throw new UnsupportedOperationException("cannot add observer twice");
	}
	
	@Override
	public MultiplexObserver<T> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked")
		MultiplexObserver<T> cl = (MultiplexObserver<T>) super.clone();
		cl.observers = new HashSet<Observer<? super T>>(observers);
		return cl;
	}
	
	/**
	 */
	@Override
	public void removeObserver(Observer<? super T> o) {
		// fail-fast behavior
		if (o == null)
			throw new IllegalArgumentException("observer must not be null");
		if (!observers.remove(o))
			throw new UnsupportedOperationException("attempt to remove unregistered observer");
	}

	/**
	 */
	@Override
	public void notify(T event) {
		for (Observer<? super T> o : observers)
			o.notify(event);
	}

}
