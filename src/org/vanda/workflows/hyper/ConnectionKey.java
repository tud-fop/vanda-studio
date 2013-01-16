package org.vanda.workflows.hyper;

import org.vanda.util.TokenSource.Token;

// use this to store target port information
// implements equals and hashCode to make it usable for sets
public final class ConnectionKey {
	// public Token address;
	// public final Token source;
	// public final int sourcePort;
	// public final Token variable;
	public final Token target;
	public final int targetPort;

	public ConnectionKey(/*Token source, int sourcePort, Token variable*/ Token target, int targetPort) {
		// address = null;
		// this.source = source;
		// this.sourcePort = sourcePort;
		// this.variable = variable;
		this.target = target;
		this.targetPort = targetPort;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ConnectionKey) {
			// address is assumed to be interned, so compare by reference
			return ((ConnectionKey) other).target == target
					&& ((ConnectionKey) other).targetPort == targetPort;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return target.hashCode() + targetPort;
	}
	
	@Override
	public String toString() {
		return Integer.toString(targetPort) + "@" + target.toString();
	}
}
