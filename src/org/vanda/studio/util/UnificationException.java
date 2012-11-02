package org.vanda.studio.util;

import org.vanda.studio.util.TokenSource.Token;

public final class UnificationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Token addr;

	public UnificationException(Token addr) {
		super("Unification failed @ " + addr.intValue());
		this.addr = addr;
	}

	public Token getAddress() {
		return addr;
	}

}
