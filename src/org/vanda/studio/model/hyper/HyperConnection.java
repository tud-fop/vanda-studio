package org.vanda.studio.model.hyper;

import org.vanda.studio.model.generation.Connection;

public class HyperConnection<F> extends Connection<HyperJob<F>> {
	public HyperConnection(HyperJob<F> source, int sourcePort,
			HyperJob<F> target, int targetPort) {
		super(source, sourcePort, target, targetPort);
	}
}
