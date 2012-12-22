package org.vanda.studio.model.types;

public interface MergeFunction<X> {
	X merge(X x1, X x2);
}
