package org.vanda.util;

public final class Pair<T1, T2> {
	public Pair(T1 fst, T2 snd) {
		this.fst = fst;
		this.snd = snd;
	}
	
	public final T1 fst;
	public final T2 snd;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ fst: ");
		sb.append(fst);
		sb.append("; snd: ");
		sb.append(snd);
		sb.append(" }");
		return sb.toString();
	}
}
