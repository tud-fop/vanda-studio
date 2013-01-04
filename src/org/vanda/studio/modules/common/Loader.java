package org.vanda.studio.modules.common;

import org.vanda.util.Observer;

public interface Loader<T> {
	/** scans for items and notifies caller per item */
	void load(Observer<T> o);
}
