/**
 * 
 */
package org.vanda.studio.app;

import java.util.Collection;

import org.vanda.studio.util.Observable;

/**
 * @author buechse
 * 
 */
public interface Repository<T> {
	Observable<T> getAddObservable();

	Observable<T> getRemoveObservable();
	
	Observable<T> getModifyObservable();

	T getItem(String id);

	boolean containsItem(String id);

	Collection<T> getItems();
	
	void refresh();
}
