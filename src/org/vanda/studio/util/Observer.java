package org.vanda.studio.util;

/**
 * Common interface for Vanda Studio event distribution targets (Observer).
 * 
 * @author buechse
 *
 */
public interface Observer<T> {
	
	/**
	 */
	void notify(T event);
	
}
