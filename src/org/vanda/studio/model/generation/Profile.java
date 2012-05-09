package org.vanda.studio.model.generation;

import org.vanda.studio.model.workflows.RepositoryItem;

public interface Profile extends RepositoryItem {
	ArtifactFactory<?, ?, HaskellFragment, HaskellView> createHaskellAF();

	ArtifactFactory<?, ?, ShellFragment, ShellView> createShellAF();
}
