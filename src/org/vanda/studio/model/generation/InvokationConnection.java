package org.vanda.studio.model.generation;

public class InvokationConnection<T extends ArtifactConn, A extends Artifact<T>>
		extends Connection<AtomicInvokation<T, A>> {

	private final T ac;

	private final Connection<?> identity;

	public InvokationConnection(AtomicInvokation<T, A> source, int sourcePort,
			AtomicInvokation<T, A> target, int targetPort, T ac,
			Connection<?> identity) {
		super(source, sourcePort, target, targetPort);
		this.ac = ac;
		this.identity = identity;
	}

	public T getArtifactConnection() {
		return ac;
	}

	public Connection<?> getIdentity() {
		return identity;
	}
}
