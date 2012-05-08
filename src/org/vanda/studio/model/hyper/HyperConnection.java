package org.vanda.studio.model.hyper;

import org.vanda.studio.model.generation.Connection;

public class HyperConnection<V> extends Connection<HyperJob<V>> {
	
	protected HyperWorkflow<?, V> parent;

	public HyperConnection(HyperJob<V> source, int sourcePort,
			HyperJob<V> target, int targetPort) {
		super(source, sourcePort, target, targetPort);
	}
	
	public HyperWorkflow<?, V> getParent() {
		return parent;
	}

}
