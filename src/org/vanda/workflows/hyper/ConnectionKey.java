package org.vanda.workflows.hyper;

import org.vanda.workflows.elements.Port;

// use this to store target port information
// implements equals and hashCode to make it usable for sets
public final class ConnectionKey {
	public final Job target;
	public final Port targetPort;

	public ConnectionKey(Job target, Port targetPort) {
		this.target = target;
		this.targetPort = targetPort;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ConnectionKey) {
			return ((ConnectionKey) other).target == target
					&& ((ConnectionKey) other).targetPort == targetPort;
		} else
			return false;
	}

	@Override
	public int hashCode() {
		return target.hashCode() + targetPort.hashCode();
	}
	
	@Override
	public String toString() {
		return target.toString() + "." + targetPort.toString();
	}
}
