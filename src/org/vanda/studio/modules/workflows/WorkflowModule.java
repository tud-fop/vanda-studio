package org.vanda.studio.modules.workflows;

import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.fragment.bash.RootLinker;
import org.vanda.fragment.bash.ShellCompiler;
import org.vanda.fragment.impl.GeneratorImpl;
import org.vanda.fragment.impl.ProfileImpl;
import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentLinker;
import org.vanda.fragment.model.Profile;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.app.SemanticsModule;
import org.vanda.studio.modules.common.ExternalRepository;
import org.vanda.studio.modules.common.ListRepository;
import org.vanda.studio.modules.workflows.impl.ShellTool;
import org.vanda.studio.modules.workflows.impl.ToolLoader;
import org.vanda.studio.modules.workflows.impl.WorkflowEditorImpl;
import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.inspector.LiteralEditor;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.run.ProfileManager;
import org.vanda.studio.modules.workflows.run.RunTool;
import org.vanda.studio.modules.workflows.run.SemanticsTool;
import org.vanda.studio.modules.workflows.run.SemanticsToolFactory;
import org.vanda.studio.modules.workflows.tools.InspectorTool;
import org.vanda.studio.modules.workflows.tools.PaletteTool;
import org.vanda.studio.modules.workflows.tools.SaveTool;
import org.vanda.studio.modules.workflows.tools.WorkflowToPDFToolFactory;
import org.vanda.util.Action;
import org.vanda.util.CompositeRepository;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.MetaRepository;
import org.vanda.util.Observer;
import org.vanda.util.Repository;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.elements.ToolInterface;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.Serialization;

public class WorkflowModule implements Module {

	@Override
	public Object createInstance(Application a) {
		return new WorkflowModuleInstance(a);
	}

	@Override
	public String getName() {
		return "Workflows"; // Module for Vanda Studio";
	}

	protected static final class WorkflowModuleInstance implements
			SemanticsModule {

		private final Application app;
		private final ElementEditorFactories eefs;
		private final ListRepository<Profile> repository;
		private final Profile profile;
		private ProfileManager manager;
		private final MetaRepository<Tool> tools;
		private final ListRepository<ToolFactory> toolFactories;

		public static String TOOL_PATH_KEY = "profileToolPath";
		public static String TOOL_PATH_DEFAULT = System
				.getProperty("user.home") + "/.vanda/functions/";

		@SuppressWarnings("unchecked")
		public WorkflowModuleInstance(Application a) {
			app = a;
			profile = new ProfileImpl();
			ListRepository<FragmentCompiler> compilers = new ListRepository<FragmentCompiler>();
			compilers.addItem(new ShellCompiler());
			profile.getFragmentCompilerMetaRepository()
					.addRepository(compilers);
			ListRepository<FragmentLinker> linkers = new ListRepository<FragmentLinker>();
			linkers.addItem(new RootLinker());
			profile.getFragmentLinkerMetaRepository().addRepository(linkers);
			repository = new ListRepository<Profile>();
			repository.addItem(profile);
			manager = null;

			tools = new CompositeRepository<Tool>();
			ToolInterface ti = new ToolInterface() {
				ExternalRepository<ShellTool> er;

				{
					String path = app.getProperty(TOOL_PATH_KEY);
					if (path == null) {
						path = TOOL_PATH_DEFAULT;
						app.setProperty(TOOL_PATH_KEY, TOOL_PATH_DEFAULT);
					}
					er = new ExternalRepository<ShellTool>(new ToolLoader(app,
							path, this));

				}

				@Override
				public String getCategory() {
					return "Generic Tool Interface";
				}

				@Override
				public String getContact() {
					return "Matthias.Buechse@tu-dresden.de";
				}

				@Override
				public String getDescription() {
					return "This is a generic tool interface. It only exists "
							+ "for a transition period until we have real tool "
							+ "interfaces.";
				}

				@Override
				public String getId() {
					return "ak4711";
				}

				@Override
				public String getName() {
					return "Generic Tool Interface";
				}

				@Override
				public String getVersion() {
					return "2013-01-08";
				}

				@Override
				public Repository<? extends Tool> getTools() {
					return er;
				}

			};
			profile.getFragmentToolMetaRepository().addRepository((Repository<ShellTool>) ti.getTools());
			tools.addRepository(ti.getTools());
			ti.getTools().refresh();

			eefs = new ElementEditorFactories();
			eefs.workflowFactories
					.add(new org.vanda.studio.modules.workflows.inspector.WorkflowEditor());
			eefs.literalFactories.add(new LiteralEditor());

			ListRepository<SemanticsToolFactory> srep = new ListRepository<SemanticsToolFactory>();
			srep.addItem(new RunTool(new GeneratorImpl(app, profile)));
			srep.addItem(new org.vanda.studio.modules.workflows.run.InspectorTool());

			toolFactories = new ListRepository<ToolFactory>();
			toolFactories.addItem(new InspectorTool(eefs));
			toolFactories.addItem(new PaletteTool());
			toolFactories.addItem(new SaveTool());
			toolFactories.addItem(new WorkflowToPDFToolFactory(app));
			toolFactories.addItem(new SemanticsTool(srep));

			app.getWindowSystem()
					.addAction(null, new OpenManagerAction(), null);
			app.getWindowSystem().addAction(null, new OpenWorkflowAction(),
					KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
			app.getWindowSystem().addAction(null, new NewWorkflowAction(),
					KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
		}

		public MetaRepository<Tool> getToolMetaRepository() {
			return tools;
		}

		protected class NewWorkflowAction implements Action {
			@Override
			public String getName() {
				return "New Workflow";
			}

			@Override
			public void invoke() {
				MutableWorkflow mwf = new MutableWorkflow("Workflow");
				new WorkflowEditorImpl(app, toolFactories, mwf, /*
																 * app .
																 * getSemanticsModuleMetaRepository
																 * (
																 * ).getRepository
																 * () .getItem(
																 * "profile")
																 */
				WorkflowModuleInstance.this);
			}
		}

		protected class OpenWorkflowAction implements Action {
			@Override
			public String getName() {
				return "Open Workflow...";
			}

			@Override
			public void invoke() {
				// create a new file opening dialog
				JFileChooser chooser = new JFileChooser("");
				chooser.setDialogType(JFileChooser.OPEN_DIALOG);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter(
						"Hyperworkflows (*.hwf)", "hwf"));
				String lastDir = app.getProperty("lastDir");
				if (lastDir != null)
					chooser.setCurrentDirectory(new File(lastDir));
				chooser.setVisible(true);
				int result = chooser.showOpenDialog(null);

				// once file choice is approved, load the chosen file
				if (result == JFileChooser.APPROVE_OPTION) {
					File chosenFile = chooser.getSelectedFile();
					app.setProperty("lastDir", chosenFile.getParentFile()
							.getAbsolutePath());
					String filePath = chosenFile.getPath();
					MutableWorkflow hwf;
					try {
						SemanticsModule prof = WorkflowModuleInstance.this;
						/*
						 * app .getSemanticsModuleMetaRepository()
						 * .getRepository().getItem("profile");
						 */
						Serialization ser = new Serialization(prof
								.getToolMetaRepository().getRepository());
						hwf = ser.load(filePath);
						new WorkflowEditorImpl(app, toolFactories, hwf, prof);
					} catch (Exception e) {
						app.sendMessage(new ExceptionMessage(e));
					}
				}
			}
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

	}
}
