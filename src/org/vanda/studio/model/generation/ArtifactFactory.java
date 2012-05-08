package org.vanda.studio.model.generation;

public interface ArtifactFactory<T extends ArtifactConn, A extends Artifact<T>, F, V> {
	F compose(InvokationWorkflow<T, A, F> invwf);

	/**
	 * Creates an artifact that computes identity. In Haskell notation, that is:
	 * id :: a -> a
	 * 
	 * @return
	 */
	A createIdentity();

	A visit(V v);
}
