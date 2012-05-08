package org.vanda.studio.util;

public final class Pair<T1, T2> {
	public Pair(T1 fst, T2 snd) {
		this.fst = fst;
		this.snd = snd;
	}
	
	public final T1 fst;
	public final T2 snd;
}
