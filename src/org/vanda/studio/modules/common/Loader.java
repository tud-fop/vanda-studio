package org.vanda.studio.modules.common;

import org.vanda.studio.util.Observer;

public interface Loader<T> {
	/** scans for items and notifies caller per item */
	void load(Observer<T> o);
}
