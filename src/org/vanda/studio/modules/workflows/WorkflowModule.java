package org.vanda.studio.modules.workflows;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.fragment.bash.RootLinker;
import org.vanda.fragment.bash.ShellCompiler;
import org.vanda.fragment.bash.ShellTool;
import org.vanda.fragment.bash.ToolLoader;
import org.vanda.fragment.impl.GeneratorImpl;
import org.vanda.fragment.impl.ProfileImpl;
import org.vanda.fragment.model.FragmentCompiler;
import org.vanda.fragment.model.FragmentLinker;
import org.vanda.fragment.model.Profile;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.modules.workflows.impl.WorkflowEditorImpl;
import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.inspector.LiteralEditor;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.run.InspectorTool;
import org.vanda.studio.modules.workflows.run.ProfileManager;
import org.vanda.studio.modules.workflows.run.SemanticsTool;
import org.vanda.studio.modules.workflows.run.SemanticsToolFactory;
import org.vanda.studio.modules.workflows.tools.AssignmentSwitchToolFactory;
import org.vanda.studio.modules.workflows.tools.AssignmentTableToolFactory;
import org.vanda.studio.modules.workflows.tools.PaletteTool;
import org.vanda.studio.modules.workflows.tools.SaveTool;
import org.vanda.studio.modules.workflows.tools.WorkflowToPDFToolFactory;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.ExternalRepository;
import org.vanda.util.ListRepository;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.serialization.Loader;

public class WorkflowModule implements Module {

	@Override
	public Object createInstance(Application a) {
		return new WorkflowModuleInstance(a);
	}

	@Override
	public String getName() {
		return "Workflows"; // Module for Vanda Studio";
	}

	protected static final class WorkflowModuleInstance {

		private final Application app;
		private final ElementEditorFactories eefs;
		private final ListRepository<Profile> repository;
		private final Profile profile;
		private ProfileManager manager;
		private final LinkedList<ToolFactory> toolFactories;

		public static String TOOL_PATH_KEY = "profileToolPath";
		public static String TOOL_PATH_DEFAULT = System
				.getProperty("user.home") + "/.vanda/functions/";

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

			ExternalRepository<ShellTool> er;
			String path = app.getProperty(TOOL_PATH_KEY);
			if (path == null) {
				path = TOOL_PATH_DEFAULT;
				app.setProperty(TOOL_PATH_KEY, TOOL_PATH_DEFAULT);
			}
			er = new ExternalRepository<ShellTool>(new ToolLoader(path));
			profile.getFragmentToolMetaRepository().addRepository(er);
			er.refresh();

			eefs = new ElementEditorFactories();
			eefs.workflowFactories
					.add(new org.vanda.studio.modules.workflows.inspector.WorkflowEditor());
			eefs.literalFactories.add(new LiteralEditor(app));

			LinkedList<SemanticsToolFactory> srep = new LinkedList<SemanticsToolFactory>();
//			srep.add(new RunTool(new GeneratorImpl(app, profile)));
			srep.add(new InspectorTool(eefs));
			srep.add(new org.vanda.studio.modules.workflows.run2.RunTool(new GeneratorImpl(app,profile)));

			toolFactories = new LinkedList<ToolFactory>();
			toolFactories.add(new PaletteTool());
			toolFactories.add(new SaveTool());
			toolFactories.add(new WorkflowToPDFToolFactory(app));
			toolFactories.add(new SemanticsTool(srep));
			toolFactories.add(new AssignmentTableToolFactory(eefs));
			toolFactories.add(new AssignmentSwitchToolFactory());

			app.getWindowSystem()
					.addAction(null, new OpenManagerAction(), null);
			app.getWindowSystem().addAction(null, new OpenWorkflowAction(),
					"document-open",
					KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK),1);
			app.getWindowSystem().addAction(null, new NewWorkflowAction(),
					"document-new",
					KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK),0);
		}

		protected class NewWorkflowAction implements Action {
			@Override
			public String getName() {
				return "New Workflow";
			}

			@Override
			public void invoke() {
				MutableWorkflow mwf = new MutableWorkflow("Workflow");
				Database d = new Database();
				new WorkflowEditorImpl(app, toolFactories,
						new Pair<MutableWorkflow, Database>(mwf, d));
				// something will hold a reference to it since it will be in the
				// GUI
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
						"Workflow XML (*.xwf)", "xwf"));
				String lastDir = app.getProperty("lastDir");
				if (lastDir != null)
					chooser.setCurrentDirectory(new File(lastDir));
				
				// center dialog over main window
				int result = chooser.showOpenDialog(app.getWindowSystem().getMainWindow());

				// once file choice is approved, load the chosen file
				if (result == JFileChooser.APPROVE_OPTION) {
					File chosenFile = chooser.getSelectedFile();
					app.setProperty("lastDir", chosenFile.getParentFile()
							.getAbsolutePath());
					String filePath = chosenFile.getPath();
					Pair<MutableWorkflow, Database> phd;
					try {
						phd = new Loader(app.getToolMetaRepository()
								.getRepository()).load(filePath);
						new WorkflowEditorImpl(app, toolFactories, phd);
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

	}
}
