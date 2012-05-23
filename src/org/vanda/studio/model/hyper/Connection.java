package org.vanda.studio.model.hyper;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.util.TokenSource.Token;

public final class Connection {
	public Token address;
	public final Token source;
	public final Port sourcePort;
	public final Token target;
	public final Port targetPort;

	public Connection(Token source, Port sourcePort, Token target, Port targetPort) {
		address = null;
		this.source = source;
		this.sourcePort = sourcePort;
		this.target = target;
		this.targetPort = targetPort;
	}
}
