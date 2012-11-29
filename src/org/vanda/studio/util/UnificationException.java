package org.vanda.studio.util;

import org.vanda.studio.util.TokenSource.Token;

public final class UnificationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Pair<Token, Integer> addr;

	public UnificationException(Pair<Token, Integer> addr) {
		super("Unification failed @ " + addr.fst.intValue());
		this.addr = addr;
	}

	public Pair<Token, Integer> getAddress() {
		return addr;
	}

}
