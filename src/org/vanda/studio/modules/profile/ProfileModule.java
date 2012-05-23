package org.vanda.studio.modules.profile;

import java.util.Collection;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.app.Profile;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.modules.common.SimpleRepository;
import org.vanda.studio.modules.profile.concrete.HaskellCompiler;
import org.vanda.studio.modules.profile.concrete.IdentityLinker;
import org.vanda.studio.modules.profile.concrete.ShellCompiler;
import org.vanda.studio.modules.profile.model.FragmentCompiler;
import org.vanda.studio.modules.profile.model.FragmentLinker;
import org.vanda.studio.modules.profile.model.Profiles;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.Observer;

public class ProfileModule implements Module {

	@Override
	public String getName() {
		return "Simple Fragment Profile";
	}

	@Override
	public Object createInstance(Application a) {
		return new ProfileModuleInstance(a);
	}

	private static final class ProfileModuleInstance {
		private final Application app;
		private final SimpleRepository<Profile> repository;
		private final Profiles profiles;
		private ProfileManager manager;

		@SuppressWarnings("unused")
		public ProfileModuleInstance(Application app) {
			this.app = app;
			profiles = new ProfilesImpl();
			SimpleRepository<FragmentCompiler> compilers = new SimpleRepository<FragmentCompiler>(null);
			compilers.addItem(new HaskellCompiler());
			compilers.addItem(new ShellCompiler());
			profiles.getFragmentCompilerMetaRepository().addRepository(compilers);
			SimpleRepository<FragmentLinker> linkers = new SimpleRepository<FragmentLinker>(null);
			linkers.addItem(new IdentityLinker());
			profiles.getFragmentLinkerMetaRepository().addRepository(linkers);
			if (false) {
				Collection<Linker> ls = app.getLinkerMetaRepository().getRepository().getItems();
				for (Linker l : ls) {
					FragmentLinker fl = profiles.getLinker(l.getId());
					if (fl == null /*|| !fl.check(l)*/)
						throw new RuntimeException();
				}
			}
			repository = new SimpleRepository<Profile>(null);
			repository.addItem(new ProfileImpl(profiles));
			manager = null;
			app.getProfileMetaRepository().addRepository(repository);
			app.getWindowSystem().addAction(null, new OpenManagerAction());
		}

		public final class OpenManagerAction implements Action {
			@Override
			public String getName() {
				return "Manage Fragment Profiles...";
			}

			@Override
			public void invoke() {
				if (manager == null) {
					manager = new ProfileManager(app, repository);
					manager.getCloseObservable().addObserver(new CloseObserver());
				}
				manager.focus();				
			}
		}
		
		public final class CloseObserver implements Observer<ProfileManager> {
			@Override
			public void notify(ProfileManager event) {
				manager = null;
			}
		}

	}

}
