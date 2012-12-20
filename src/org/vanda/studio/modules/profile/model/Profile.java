package org.vanda.studio.modules.profile.model;

import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.profile.fragments.FragmentCompiler;
import org.vanda.studio.modules.profile.fragments.FragmentLinker;

public interface Profile extends RepositoryItem {
	
	FragmentCompiler getCompiler(Type t);
	
	MetaRepository<FragmentCompiler> getFragmentCompilerMetaRepository();
	
	MetaRepository<FragmentLinker> getFragmentLinkerMetaRepository();
	
	FragmentLinker getLinker(String id);
	
	FragmentLinker getRootLinker(Type t);
	
	Type getRootType();
	
}
