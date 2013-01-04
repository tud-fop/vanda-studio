/**
 * 
 */
package org.vanda.util;

import java.util.ArrayList;

/**
 * Common base class for event sources (Observable).
 * <p>
 * Implements the Vanda Composer event distribution infrastructure.
 * 
 * @author buechse
 * 
 */
public class MultiplexObserver<T> implements Observable<T>, Observer<T>, Cloneable {

	// XXX replaced HashSet by ArrayList because order is indeed important
	protected ArrayList<Observer<? super T>> observers;

	public MultiplexObserver() {
		observers = new ArrayList<Observer<? super T>>();
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
		cl.observers = new ArrayList<Observer<? super T>>(observers);
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
