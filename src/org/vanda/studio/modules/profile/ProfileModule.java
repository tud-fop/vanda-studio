package org.vanda.studio.modules.profile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.app.Module;
import org.vanda.studio.app.Repository;
import org.vanda.studio.app.SemanticsModule;
import org.vanda.studio.app.ToolFactory;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.types.CompositeType;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.common.CompositeRepository;
import org.vanda.studio.modules.common.ListRepository;
import org.vanda.studio.modules.common.SimpleRepository;
import org.vanda.studio.modules.profile.concrete.HaskellCompiler;
import org.vanda.studio.modules.profile.concrete.HaskellLinker;
import org.vanda.studio.modules.profile.concrete.IdentityLinker;
import org.vanda.studio.modules.profile.concrete.ShellCompiler;
import org.vanda.studio.modules.profile.model.FragmentCompiler;
import org.vanda.studio.modules.profile.model.FragmentLinker;
import org.vanda.studio.modules.profile.model.Profiles;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.ExceptionMessage;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource;

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
		private final SimpleRepository<Profile> repository;
		private final Profiles profiles;
		private ProfileManager manager;
		private final MetaRepository<Tool> tools;

		public static String TOOL_PATH_KEY = "profileToolPath";
		public static String TOOL_PATH_DEFAULT = System
				.getProperty("user.home") + "/.vanda/functions/";

		@SuppressWarnings("unused")
		public ProfileModuleInstance(Application app) {
			this.app = app;
			profiles = new ProfilesImpl();
			SimpleRepository<FragmentCompiler> compilers = new SimpleRepository<FragmentCompiler>(
					null);
			compilers.addItem(new HaskellCompiler());
			compilers.addItem(new ShellCompiler());
			profiles.getFragmentCompilerMetaRepository().addRepository(
					compilers);
			SimpleRepository<FragmentLinker> linkers = new SimpleRepository<FragmentLinker>(
					null);
			linkers.addItem(new IdentityLinker());
			linkers.addItem(new HaskellLinker());
			profiles.getFragmentLinkerMetaRepository().addRepository(linkers);
			if (false) {
				Collection<Linker> ls = app.getLinkerMetaRepository()
						.getRepository().getItems();
				for (Linker l : ls) {
					FragmentLinker fl = profiles.getLinker(l.getId());
					if (fl == null /* || !fl.check(l) */)
						throw new RuntimeException();
				}
			}
			repository = new SimpleRepository<Profile>(null);
			repository.addItem(new ProfileImpl(app, profiles));
			manager = null;
			// app.getProfileMetaRepository().addRepository(repository);

			ListRepository<ToolFactory> rep = new ListRepository<ToolFactory>();
			rep.addItem(new RunTool(repository.getItem("fragment-profile")));
			rep.addItem(new InspectorTool(profiles));
			app.getToolFactoryMetaRepository().addRepository(rep);

			tools = new CompositeRepository<Tool>();
			String path = app.getProperty(TOOL_PATH_KEY);
			if (path == null) {
				path = TOOL_PATH_DEFAULT;
				app.setProperty(TOOL_PATH_KEY, TOOL_PATH_DEFAULT);
			}
			for (File f : (new File(path)).listFiles()) {
				if (f.isFile() && f.getAbsolutePath().endsWith(".bash"))
					loadFromFile(f);
			}
			app.setSemanticsModule(this);

			app.getWindowSystem()
					.addAction(null, new OpenManagerAction(), null);
		}

		public void loadFromFile(File file) {
			Set<String> imports = new HashSet<String>();
			imports.add(file.getAbsolutePath());
			SimpleRepository<Tool> r = new SimpleRepository<Tool>(null);
			Scanner sc = null;
			TokenSource ts = new TokenSource();
			Map<String, Type> tVars = new HashMap<String, Type>();
			try {
				sc = new Scanner(file);
				boolean nameFound = false;
				String id = "";
				String name = "";
				String description = "";
				String version = "";
				String category = "";
				String contact = "";
				List<Port> inPorts = new ArrayList<Port>();
				List<Port> outPorts = new ArrayList<Port>();
				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					if (line.startsWith("#")) {
						String line1 = line.substring(1).trim();
						if (!nameFound) {
							name = line1;
							nameFound = true;
						} else if (line1.toLowerCase().startsWith("version"))
							version = line1.split(":")[1].trim();
						else if (line1.toLowerCase().startsWith("contact"))
							contact = line1.split(":")[1].trim();
						else if (line1.toLowerCase().startsWith("category"))
							category = line1.split(":")[1].trim();
						else if (line1.toLowerCase().startsWith("in")) {
							String[] arr = line1.substring(2).trim()
									.split("::");
							Type t = ShellTool.parseType(tVars, ts, arr[1].trim());
							inPorts.add(new Port(arr[0].trim(), t));
						} else if (line1.toLowerCase().startsWith("out")) {
							String[] arr = line1.substring(3).trim()
									.split("::");
							Type t = ShellTool.parseType(tVars, ts, arr[1].trim());
							outPorts.add(new Port(arr[0].trim(), t));
						} else if (description != null && line1 == "")
							description = null;
						else
							description = line1;
					} else if (line.matches(".*\\(\\).*\\{")) {
						id = line.trim().split(" ")[0];
						if (!name.equals("")) {
							List<Port> in = new ArrayList<Port>();
							in.addAll(inPorts);
							inPorts.clear();
							List<Port> out = new ArrayList<Port>();
							out.addAll(outPorts);
							outPorts.clear();
							Tool t = new ShellTool(new String(id), new String(
									name), new String(category), new String(
									version), new String(contact), new String(
									description), in, out, imports);
							r.addItem(t);
							nameFound = false;
							ts = new TokenSource();
							tVars.clear();
						}
					}
				}
			} catch (FileNotFoundException e) {
				app.sendMessage(new ExceptionMessage(new Exception("Tool file "
						+ file.getAbsolutePath() + " can not be loaded.")));
			} finally {
				sc.close();
			}
			tools.addRepository(r);
		}

		public MetaRepository<Tool> getTools() {
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
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getContact() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getVersion() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void visit(RepositoryItemVisitor v) {
			// TODO Auto-generated method stub

		}

	}

}
