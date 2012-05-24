package org.vanda.studio.modules.profile;

import java.util.Iterator;

import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.model.types.Types;
import org.vanda.studio.modules.common.CompositeRepository;
import org.vanda.studio.modules.profile.model.FragmentCompiler;
import org.vanda.studio.modules.profile.model.FragmentLinker;
import org.vanda.studio.modules.profile.model.Profiles;

public final class ProfilesImpl implements Profiles {

	private final MetaRepository<FragmentCompiler> fragmentCompilers;
	private final MetaRepository<FragmentLinker> fragmentLinkers;

	public ProfilesImpl() {
		fragmentCompilers = new CompositeRepository<FragmentCompiler>();
		fragmentLinkers = new CompositeRepository<FragmentLinker>();
	}

	@Override
	public MetaRepository<FragmentCompiler> getFragmentCompilerMetaRepository() {
		return fragmentCompilers;
	}

	@Override
	public MetaRepository<FragmentLinker> getFragmentLinkerMetaRepository() {
		return fragmentLinkers;
	}

	@Override
	public FragmentCompiler getCompiler(Type t) {
		// XXX optimization: do not compute a fresh copy of t in each iteration
		FragmentCompiler result = null;
		Iterator<FragmentCompiler> it = fragmentCompilers.getRepository()
				.getItems().iterator();
		while (it.hasNext() && result == null) {
			FragmentCompiler fc = it.next();
			if (Types.canUnify(t, fc.getFragmentType()))
				result = fc;
		}
		return result;
	}

	@Override
	public FragmentLinker getLinker(String id) {
		return fragmentLinkers.getRepository().getItem(id);
	}

}
