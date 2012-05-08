package org.vanda.studio.model.generation;

public interface Profile {
	ArtifactFactory<?, ?, HaskellFragment, HaskellView> createHaskellAF();

	ArtifactFactory<?, ?, ShellFragment, ShellView> createShellAF();
}
