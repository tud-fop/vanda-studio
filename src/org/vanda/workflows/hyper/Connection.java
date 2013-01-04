package org.vanda.workflows.hyper;

import org.vanda.util.TokenSource.Token;

public final class Connection {
	public Token address;
	public final Token source;
	public final int sourcePort;
	public final Token target;
	public final int targetPort;

	public Connection(Token source, int sourcePort, Token target, int targetPort) {
		address = null;
		this.source = source;
		this.sourcePort = sourcePort;
		this.target = target;
		this.targetPort = targetPort;
	}
}
