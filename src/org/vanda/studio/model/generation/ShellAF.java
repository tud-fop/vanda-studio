package org.vanda.studio.model.generation;

import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;

public interface ShellAF<T extends ArtifactConn, A extends Artifact<T>> {
	A fromHaskell(HaskellFragment fragment);

	A createNano();
}
