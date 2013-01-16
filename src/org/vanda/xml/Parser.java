package org.vanda.xml;

public interface Parser<T> {
	void notify(T ti);
	RuntimeException fail(Throwable e);
}
