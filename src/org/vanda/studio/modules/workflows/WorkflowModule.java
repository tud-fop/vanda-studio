package org.vanda.studio.modules.workflows;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.model.generation.Artifact;
import org.vanda.studio.model.generation.ArtifactConn;
import org.vanda.studio.model.generation.ArtifactFactory;
import org.vanda.studio.model.generation.InvokationWorkflow;
import org.vanda.studio.model.generation.Port;
import org.vanda.studio.model.generation.Profile;
import org.vanda.studio.model.generation.ShellFragment;
import org.vanda.studio.model.generation.ShellView;
import org.vanda.studio.model.hyper.HyperWorkflow;
import org.vanda.studio.model.hyper.Serialization;
import org.vanda.studio.model.workflows.Compiler;
import org.vanda.studio.model.workflows.Linker;
import org.vanda.studio.model.workflows.RendererAssortment;
import org.vanda.studio.model.workflows.Tool;
import org.vanda.studio.model.workflows.ToolInstance;
import org.vanda.studio.modules.common.SimpleRepository;
import org.vanda.studio.modules.common.SimpleToolInstance;
import org.vanda.studio.util.Action;

public class WorkflowModule implements Module {

	@Override
	public Object createInstance(Application a) {
		return new WorkflowModuleInstance(a);
	}

	@Override
	public String getName() {
		return "Workflows"; // Module for Vanda Studio";
	}

	protected static class WorkflowModuleInstance {

		Application app;

		public WorkflowModuleInstance(Application a) {
			app = a;

			app.getWindowSystem().addSeparator();			
			
			// BEGIN TEMPORARY CODE
			//XXX adding three tools to repository for testing purposes
			SimpleRepository<Tool<ShellView,ToolInstance>> tr 
				= new SimpleRepository<Tool<ShellView,ToolInstance>>(
						null);
			
			Tool<ShellView, ToolInstance> sourceTool = new Tool<ShellView,ToolInstance>() {	
				public <T extends ArtifactConn, A extends Artifact<T>, F> A createArtifact(
						ArtifactFactory<T, A, F, ShellView> af, ToolInstance instance) {
					return af.createIdentity();
				}
				public ToolInstance createInstance() {
					return new SimpleToolInstance();
				}
				public String getContact() {
					return "afischer";
				}
				public String getCategory() {
					return "testCategory";
				}
				public String getVersion() {
					return "date";
				}
				public String getDescription() {
					return "TestSource";
				}
				public String getId() {
					return "sourceId";
				}
				public List<Port> getInputPorts() {
					return new ArrayList<Port>();
				}
				public String getName() {
					return "Source";
				}
				public List<Port> getOutputPorts() {
					Port p = new Port("outputPort", "portType");
					List<Port> list = new ArrayList<Port>();
					list.add(p);
					return list;
				}
				public Class<ShellView> getViewType() {
					return ShellView.class;
				}
				public <R> R selectRenderer(RendererAssortment<R> ra) {
					return ra.selectSinkRenderer();
				}
				public void appendActions(List<Action> as) {
				}
			};
			
			Tool<ShellView, ToolInstance> algoTool = new Tool<ShellView,ToolInstance>() {	
				public <T extends ArtifactConn, A extends Artifact<T>, F> A createArtifact(
						ArtifactFactory<T, A, F, ShellView> af, ToolInstance instance) {
					return af.createIdentity();
				}
				public ToolInstance createInstance() {
					return new SimpleToolInstance();
				}
				public String getContact() {
					return "afischer";
				}
				public String getCategory() {
					return "testCategory";
				}
				public String getVersion() {
					return "date";
				}
				public String getDescription() {
					return "TestAlgo";
				}
				public String getId() {
					return "algoId";
				}
				public List<Port> getInputPorts() {
					Port p = new Port("inputPort", "portType");
					List<Port> list = new ArrayList<Port>();
					list.add(p);
					return list;
				}
				public String getName() {
					return "Algo";
				}
				public List<Port> getOutputPorts() {
					Port p = new Port("outputPort", "portType");
					List<Port> list = new ArrayList<Port>();
					list.add(p);
					return list;
				}
				public Class<ShellView> getViewType() {
					return ShellView.class;
				}
				public <R> R selectRenderer(RendererAssortment<R> ra) {
					return ra.selectSinkRenderer();
				}
				public void appendActions(List<Action> as) {
				}
			};
			
			Tool<ShellView, ToolInstance> sinkTool = new Tool<ShellView,ToolInstance>() {	
				public <T extends ArtifactConn, A extends Artifact<T>, F> A createArtifact(
						ArtifactFactory<T, A, F, ShellView> af, ToolInstance instance) {
					return af.createIdentity();
				}
				public ToolInstance createInstance() {
					return new SimpleToolInstance();
				}
				public String getContact() {
					return "afischer";
				}
				public String getCategory() {
					return "testCategory";
				}
				public String getVersion() {
					return "date";
				}
				public String getDescription() {
					return "TestSink";
				}
				public String getId() {
					return "sinkId";
				}
				public List<Port> getInputPorts() {
					Port p = new Port("inputPort", "portType");
					List<Port> list = new ArrayList<Port>();
					list.add(p);
					return list;
				}
				public String getName() {
					return "Sink";
				}
				public List<Port> getOutputPorts() {
					return new ArrayList<Port>();
				}
				public Class<ShellView> getViewType() {
					return ShellView.class;
				}
				public <R> R selectRenderer(RendererAssortment<R> ra) {
					return ra.selectSinkRenderer();
				}
				public void appendActions(List<Action> as) {
				}
			};
			
			tr.addItem(sourceTool);
			tr.addItem(algoTool);
			tr.addItem(sinkTool);
			app.getToolRR().addRepository(tr);
			
			//XXX adding compiler to repository for testing purposes
			SimpleRepository<Compiler<Object, ShellView>> cr 
			= new SimpleRepository<Compiler<Object, ShellView>>(
					null);
			
			Compiler<Object, ShellView> compiler = new Compiler<Object, ShellView>() {

				public String getContact() {
					return "buechse";
				}
				
				@Override
				public String getId() {
					return "compilerId";
				}
				
				public String getName() {
					return "myCompiler";
				}
				
				public String getVersion() {
					return "version";
				}
				
				@Override
				public ArtifactFactory<?, ?, Object, ShellView> createArtifactFactory(
						Profile profile) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Class<Object> getFragmentType() {
					// TODO Auto-generated method stub
					return Object.class;
				}

				@Override
				public Class<ShellView> getViewType() {
					return ShellView.class;
				}};
			
			cr.addItem(compiler);
			app.getCompilerRR().addRepository(cr);
			
			//XXX adding linker to repository for testing purposes
			SimpleRepository<Linker<ShellFragment, ShellView, ToolInstance>> lr 
			= new SimpleRepository<Linker<ShellFragment, ShellView, ToolInstance>>(null);
			
			Linker<ShellFragment, ShellView, ToolInstance> linker 
			= new Linker<ShellFragment, ShellView, ToolInstance>() {
				
				@Override
				public String getContact() {
					return "afischer";
				}
				
				@Override
				public String getId() {
					return "linkerId";
				}
				
				@Override
				public String getName() {
					return "myLinker";
				}
				
				@Override
				public String getVersion() {
					return "version";
				}
				
				@Override
				public void appendActions(List<Action> as) {
				}
				
				@Override
				public <T extends ArtifactConn, A extends Artifact<T>> A link(
						ArtifactFactory<T, A, ?, ShellView> af, InvokationWorkflow<?, ?, ShellFragment> pre,
						ToolInstance instance) {
					return af.createIdentity();
				}
				
				@Override
				public boolean checkInputTypes(List<String> outer, List<String> inner) {
					return true;
				}
				
				@Override
				public boolean checkOutputTypes(List<String> outer, List<String> inner) {
					return true;
				}
				
				@Override
				public List<Port> convertInputPorts(List<Port> ips) {
					return ips;
				}
				
				@Override
				public List<Port> convertOutputPorts(List<Port> ops) {
					return ops;
				}
				
				@Override
				public ToolInstance createInstance() {
					return new SimpleToolInstance();
				}

				@Override
				public Class<ShellFragment> getFragmentType() {
					return ShellFragment.class;
				}

				@Override
				public Class<ShellView> getViewType() {
					return ShellView.class;
				}
			};
			
			lr.addItem(linker);
			app.getLinkerRR().addRepository(lr);
			//END of testing code
			
			/*
			 * Action save = new SaveWorkflowAction(editor);
			 * app.getWindowSystem().addAction(save);
			 * app.getWindowSystem().disableAction(save);
			 * 
			 * app.getWindowSystem().addSeparator();
			 * 
			 * Action close = new CloseWorkflowAction(editor);
			 * app.getWindowSystem().addAction(close);
			 * app.getWindowSystem().disableAction(close);
			 * 
			 * app.getWindowSystem().addSeparator();
			 * 
			 * 
			 */
			app.getWindowSystem().addAction(new OpenWorkflowAction());
			app.getWindowSystem().addAction(new NewWorkflowAction());
		}
		
		/*
		 * determines the active WorkflowEditorTab of the specified
		 * WorkflowEditor
		 * 
		 * @param we
		 * @return
		 *
		protected static WorkflowEditorTab getActiveTab(WorkflowEditor we) {
			JTabbedPane tabbedPane = (JTabbedPane) we.tabs.values().iterator()
					.next().getComponent().getParent();
			JSplitPane splitPane = (JSplitPane) tabbedPane
					.getSelectedComponent();

			WorkflowEditorTab weTab = null;
			for (String s : we.tabs.keySet()) {
				if (we.tabs.get(s).mainpane.equals(splitPane)) {
					weTab = we.tabs.get(s);
				}
			}

			return weTab;
		}

		protected static class CloseWorkflowAction implements Action {

			protected Editor<VWorkflow> editor;

			public CloseWorkflowAction(Editor<VWorkflow> e) {
				this.editor = e;
			}

			@Override
			public String getName() {
				return "Close Hyperworkflow";
			}

			@Override
			public void invoke() {
				WorkflowEditor we = (WorkflowEditor) editor;

				WorkflowEditorTab weTab = getActiveTab(we);
				if (weTab != null) {
					we.close(weTab.vworkflow);
				} else {
					assert (false);
				}
			}
		}*/

		protected class NewWorkflowAction implements Action {
			@Override
			public String getName() {
				return "New Hyperworkflow";
			}

			@Override
			public void invoke() {				
				new WorkflowEditor(app, new HyperWorkflow(app.getCompilerRepository().getItem("compilerId")));
			}
		}

		protected class OpenWorkflowAction implements Action {
			@Override
			public String getName() {
				return "Open Hyperworkflow";
			}

			@Override
			public void invoke() {
				// create a new file opening dialog
				JFileChooser chooser = new JFileChooser("");
				chooser.setDialogType(JFileChooser.OPEN_DIALOG);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter(
						"Nested Hyperworkflows (*.nhwf)", "nhwf"));
				chooser.setVisible(true);
				int result = chooser.showOpenDialog(null);

				// once file choice is approved, load the chosen file
				if (result == JFileChooser.APPROVE_OPTION) {
					File chosenFile = chooser.getSelectedFile();
					String filePath = chosenFile.getPath();

					new WorkflowEditor(app, Serialization.load(filePath, app));
				}
			}
		}
		
		/*
		protected static class SaveWorkflowAction implements Action {

			protected Editor<VWorkflow> editor;

			public SaveWorkflowAction(Editor<VWorkflow> e) {
				this.editor = e;
			}

			@Override
			public String getName() {
				return "Save Workflow";
			}

			@Override
			public void invoke() {
				// create a new file opening dialog
				JFileChooser chooser = new JFileChooser("") {
					@Override
					public void approveSelection() {
						File f = getSelectedFile();
						if (f.exists() && getDialogType() == SAVE_DIALOG) {
							int result = JOptionPane.showConfirmDialog(this,
									"The file exists already. Replace?",
									"Existing file",
									JOptionPane.YES_NO_CANCEL_OPTION);
							switch (result) {
							case JOptionPane.YES_OPTION:
								super.approveSelection();
								return;
							case JOptionPane.NO_OPTION:
								return;
							case JOptionPane.CANCEL_OPTION:
								cancelSelection();
								return;
							default:
								return;
							}
						}
						super.approveSelection();
					}
				};

				chooser.setDialogType(JFileChooser.SAVE_DIALOG);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter(
						"Nested Hyperworkflows (*.nhwf)", "nhwf"));
				chooser.setVisible(true);
				int result = chooser.showSaveDialog(null);

				// once file choice is approved, save the chosen file
				if (result == JFileChooser.APPROVE_OPTION) {
					File chosenFile = chooser.getSelectedFile();
					String filePath = chosenFile.getPath();

					WorkflowEditorTab weTab = getActiveTab((WorkflowEditor) editor);
					if (weTab != null) {
						weTab.nhwf.save(filePath);
						System.out.println("TODO: saved to file " + filePath);
					} else {
						assert (false);
					}
				}
			}
		}*/
	}
}