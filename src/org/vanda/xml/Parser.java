package org.vanda.xml;

import org.vanda.util.Observer;

public interface Parser<T> {
	Observer<T> getObserver();
	// void notify(T ti);
	RuntimeException fail(Throwable e);
}
