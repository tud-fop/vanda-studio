package org.vanda.studio.modules.workflows.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Action;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleModule;
import org.vanda.studio.modules.common.SimpleModuleInstance;
import org.vanda.studio.modules.common.ToolFactory;
import org.vanda.studio.modules.workflows.gui.WorkflowEditor.WorkflowEditorTab;

public class WorkflowModule implements SimpleModule<VWorkflow> {

	@Override
	public Editor<VWorkflow> createEditor(Application app) {
		return new WorkflowEditor(app);
	}

	@Override
	public ToolFactory<VWorkflow> createFactory() {
		return new VWorkflowFactory();
	}

	@Override
	public ModuleInstance<VWorkflow> createInstance(Application app) {
		return new WorkflowModuleInstance(app, this);
	}

	@Override
	public String getExtension() {
		return ".nhwf";
	}

	@Override
	public String getName() {
		return "Workflows"; // Module for Vanda Studio";
	}

	protected static class WorkflowModuleInstance extends
			SimpleModuleInstance<VWorkflow> {

		public WorkflowModuleInstance(Application a, SimpleModule<VWorkflow> m) {
			super(a, m);

			app.getWindowSystem().addSeparator();
			
			Action save = new SaveWorkflowAction(editor);
			app.getWindowSystem().addAction(save);
			app.getWindowSystem().disableAction(save);

			app.getWindowSystem().addSeparator();

			Action close = new CloseWorkflowAction(editor);
			app.getWindowSystem().addAction(close);
			app.getWindowSystem().disableAction(close);

			app.getWindowSystem().addSeparator();

			app.getWindowSystem().addAction(new OpenWorkflowAction());
			app.getWindowSystem().addAction(new NewWorkflowAction());
		}

		/** determines the active WorkflowEditorTab of the 
		 * 	specified WorkflowEditor
		 * 
		 * @param we
		 * @return
		 */
		protected static WorkflowEditorTab getActiveTab(WorkflowEditor we) {
			JTabbedPane tabbedPane = (JTabbedPane) we.tabs.values()
				.iterator().next().getComponent().getParent();
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
					assert(false);
				}
			}
		}

		protected class NewWorkflowAction implements Action {
			@Override
			public String getName() {
				return "New Hyperworkflow";
			}

			@Override
			public void invoke() {
				// create term (file)
				VWorkflow t = factory.createInstance(
						WorkflowModuleInstance.this, null);
				// do something with the repository
				// repository.addItem(t); FIXME
				// open editor for term
				openEditor(t);
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

					// create term (file)
					VWorkflow t = factory.createInstance(
							WorkflowModuleInstance.this, new File(filePath));
					// do something with the repository
					// repository.addItem(t); FIXME
					// open editor for term
					openEditor(t);
				}
			}
		}

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
						assert(false);
					}
				}
			}
		}
	}
}