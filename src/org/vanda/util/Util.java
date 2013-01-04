package org.vanda.util;

import java.util.Collection;
import java.util.LinkedList;

/**
 */
public class Util {
	
	public static boolean hasActions(HasActions... has) {
		return getDefaultAction(has) != null;
	}
	
	public static Action getDefaultAction(HasActions... has) {
		LinkedList<Action> as = new LinkedList<Action>();
		for (HasActions ha : has) {
			ha.appendActions(as);
			if (!as.isEmpty())
				return as.peek();
		}
		return null;
	}
	
	public static <T> void notifyAll(Observer<T> observer, Collection<? extends T> events) {
		for (T e : events)
			observer.notify(e);
	}
}
