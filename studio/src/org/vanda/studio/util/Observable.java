/**
 * 
 */
package org.vanda.studio.util;

/**
 * @author rmueller
 * 
 */
public interface Observable<T> {

	/**
	 */
	void addObserver(Observer<T> o);

	/**
	 */
	void removeObserver(Observer<T> o);

}
