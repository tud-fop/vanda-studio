package org.vanda.util;


public interface Loader<T> {
	/** scans for items and notifies caller per item */
	void load(Observer<T> o);
}
