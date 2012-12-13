package org.vanda.studio.modules.profile;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.app.Module;
import org.vanda.studio.app.SemanticsModule;
import org.vanda.studio.app.ToolFactory;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.modules.common.CompositeRepository;
import org.vanda.studio.modules.common.ExternalRepository;
import org.vanda.studio.modules.common.ListRepository;
import org.vanda.studio.modules.profile.concrete.ShellCompiler;
import org.vanda.studio.modules.profile.model.FragmentCompiler;
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

	private static final class ProfileModuleInstance implements SemanticsModule {
		private final Application app;
		private final ListRepository<Profile> repository;
		private final Profiles profiles;
		private ProfileManager manager;
		private final MetaRepository<Tool> tools;

		public static String TOOL_PATH_KEY = "profileToolPath";
		public static String TOOL_PATH_DEFAULT = System
				.getProperty("user.home") + "/.vanda/functions/";

		public ProfileModuleInstance(Application app) {
			this.app = app;
			profiles = new ProfilesImpl();
			ListRepository<FragmentCompiler> compilers = new ListRepository<FragmentCompiler>();
			compilers.addItem(new ShellCompiler());
			profiles.getFragmentCompilerMetaRepository().addRepository(
					compilers);
			repository = new ListRepository<Profile>();
			repository.addItem(new ProfileImpl(app, profiles));
			manager = null;
			// app.getProfileMetaRepository().addRepository(repository);

			ListRepository<ToolFactory> rep = new ListRepository<ToolFactory>();
			rep.addItem(new RunTool(repository.getItem("fragment-profile")));
			rep.addItem(new InspectorTool(profiles));
			app.getToolFactoryMetaRepository().addRepository(rep);

			String path = app.getProperty(TOOL_PATH_KEY);
			if (path == null) {
				path = TOOL_PATH_DEFAULT;
				app.setProperty(TOOL_PATH_KEY, TOOL_PATH_DEFAULT);
			}
			tools = new CompositeRepository<Tool>();
			ExternalRepository<Tool> er = new ExternalRepository<Tool>(new ToolLoader(app, path));
			tools.addRepository(er);
			er.refresh();
			
			ListRepository<SemanticsModule> singleton = new ListRepository<SemanticsModule>();
			singleton.addItem(this);
			app.getSemanticsModuleMetaRepository().addRepository(singleton);

			app.getWindowSystem()
					.addAction(null, new OpenManagerAction(), null);
		}

		public MetaRepository<Tool> getToolMetaRepository() {
			return tools;
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
					manager.getCloseObservable().addObserver(
							new CloseObserver());
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

		@Override
		public String getName() {
			return "Profile Semantics";
		}

		@Override
		public String getCategory() {
			return "Profile Semantics";
		}

		@Override
		public String getContact() {
			return "Matthias.Buechse@tu-dresden.de";
		}

		@Override
		public String getDescription() {
			return "Semantics module based on fragment profiles";
		}

		@Override
		public String getId() {
			return "profile";
		}

		@Override
		public String getVersion() {
			return "2012-12-12";
		}

		@Override
		public void visit(RepositoryItemVisitor v) {
			// TODO Auto-generated method stub

		}

	}

}
