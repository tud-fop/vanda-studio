/**
 * 
 */
package org.vanda.util;

import java.lang.ref.WeakReference;
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
	protected ArrayList<WeakReference<Observer<? super T>>> observers;

	public MultiplexObserver() {
		observers = new ArrayList<WeakReference<Observer<? super T>>>();
	}

	/**
	 */
	@Override
	public void addObserver(Observer<? super T> o) {
		// fail-fast behavior
		if (o == null)
			throw new IllegalArgumentException("observer must not be null");

		boolean exists = false;
		for (WeakReference<Observer<? super T>> oRef : new ArrayList<WeakReference<Observer<? super T>>>(observers))
			// lazy deletion
			if (oRef.get() == null)
				observers.remove(oRef);
			else if (o.equals(oRef.get())) {
				exists = true;
				break;
			}
		if (!exists)
			observers.add(new WeakReference<Observer<? super T>>(o));
		else
			throw new UnsupportedOperationException("cannot add observer twice");
	}

	@Override
	public MultiplexObserver<T> clone() throws CloneNotSupportedException {
		@SuppressWarnings("unchecked")
		MultiplexObserver<T> cl = (MultiplexObserver<T>) super.clone();
		cl.observers = new ArrayList<WeakReference<Observer<? super T>>>(observers);
		return cl;
	}

	/**
	 */
	@Override
	public void removeObserver(Observer<? super T> o) {
		// fail-fast behavior
		if (o == null)
			throw new IllegalArgumentException("observer must not be null");
		boolean removed = false;
		for (WeakReference<Observer<? super T>> oRef : new ArrayList<WeakReference<Observer<? super T>>>(observers)) {
			// lazy deletion
			if (oRef.get() == null)
				observers.remove(oRef);
			else if (o.equals(oRef.get())) {
				observers.remove(oRef);
				removed = true;
			}
		}
		if (!removed)
			throw new UnsupportedOperationException("attempt to remove unregistered observer");
	}

	/**
	 */
	@Override
	public void notify(T event) {
		// FIXME the following is to circumvent concurrent modification
		ArrayList<WeakReference<Observer<? super T>>> obs = new ArrayList<WeakReference<Observer<? super T>>>(observers);
		for (WeakReference<Observer<? super T>> oRef : obs) {
			Observer<? super T> o = oRef.get();
			if (o != null)
				o.notify(event);
			// lazy cleanup
			else
				observers.remove(oRef);
		}
	}

}
