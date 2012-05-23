package org.vanda.studio.modules.profile.model;

import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.model.types.Type;

public interface Profiles {
	FragmentCompiler getCompiler(Type t);
	
	MetaRepository<FragmentCompiler> getFragmentCompilerMetaRepository();
	
	MetaRepository<FragmentLinker> getFragmentLinkerMetaRepository();
	
	FragmentLinker getLinker(String id);
}
