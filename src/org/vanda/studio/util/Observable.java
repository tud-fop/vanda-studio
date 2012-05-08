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
	void addObserver(Observer<? super T> o);

	/**
	 */
	void removeObserver(Observer<? super T> o);

}
