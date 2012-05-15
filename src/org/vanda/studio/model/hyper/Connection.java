package org.vanda.studio.model.hyper;

import org.vanda.studio.util.TokenSource.Token;

public final class Connection {
	public final Token source;
	public final int sourcePort;
	public final Token target;
	public final int targetPort;

	public Connection(Token source, int sourcePort, Token target, int targetPort) {
		this.source = source;
		this.sourcePort = sourcePort;
		this.target = target;
		this.targetPort = targetPort;
	}
}
