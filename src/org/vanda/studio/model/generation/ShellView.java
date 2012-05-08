package org.vanda.studio.model.generation;

import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;

public interface ShellView {
	<T extends ArtifactConn, A extends Artifact<T>> A invoke(ShellAF<T, A> saf);
}
