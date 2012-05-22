package org.vanda.studio.modules.profile;

import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.core.CompositeRepository;

public final class ProfilesImpl implements Profiles {
	
	private final MetaRepository<FragmentLinker> fragmentLinkers;
	
	public ProfilesImpl() {
		fragmentLinkers = new CompositeRepository<FragmentLinker>();
	}

	@Override
	public MetaRepository<FragmentLinker> getFragmentLinkerMetaRepository() {
		return fragmentLinkers;
	}

}
