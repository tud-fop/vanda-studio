package org.vanda.studio.util;

import java.util.Collection;

/**
 */
public class Util {
	
	public static <T> void notifyAll(Observer<T> observer, Collection<T> events) {
		for (T e : events)
			observer.notify(e);
	}
}
