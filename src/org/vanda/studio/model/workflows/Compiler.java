package org.vanda.studio.model.workflows;

import org.vanda.studio.model.generation.ArtifactFactory;
import org.vanda.studio.model.generation.Profile;

public interface Compiler<F, V> {
	ArtifactFactory<?, ?, F, V> createArtifactFactory(Profile profile);
	
	Class<F> getFragmentType();
	
	Class<V> getViewType();
}
