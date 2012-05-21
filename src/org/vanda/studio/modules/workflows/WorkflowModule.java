package org.vanda.studio.modules.workflows;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.studio.app.Application;
import org.vanda.studio.app.Module;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.hyper.Serialization;
import org.vanda.studio.util.Action;
import org.vanda.studio.util.ExceptionMessage;

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
		private final List<ToolFactory> toolFactories;
		private final List<ElementEditorFactory> eefs;

		public WorkflowModuleInstance(Application a) {
			app = a;

			eefs = new LinkedList<ElementEditorFactory>();
			eefs.add(new WorkflowElementEditor());
			eefs.add(new LiteralEditor());
			
			toolFactories = new LinkedList<ToolFactory>();
			toolFactories.add(new DebuggerTool());
			toolFactories.add(new InspectorTool(eefs));
			toolFactories.add(new ToolFactory() {
				@Override
				public Object instantiate(WorkflowEditor wfe, Model<?> m) {
					Action a = new SaveWorkflowAction(m);
					wfe.addAction(a);
					return a;
				}
			});

			app.getWindowSystem().addAction(null, new OpenWorkflowAction());
			app.getWindowSystem().addAction(null, new NewWorkflowAction());
		}

		protected class NewWorkflowAction implements Action {
			@Override
			public String getName() {
				return "New Workflow";
			}

			@Override
			public void invoke() {
				MutableWorkflow<Object> mwf = new MutableWorkflow<Object>(
						Object.class);
				mwf.setName("Workflow");
				new WorkflowEditorImpl(app, mwf, toolFactories);
			}
		}

		protected class OpenWorkflowAction implements Action {
			@Override
			public String getName() {
				return "Open Workflow";
			}

			@Override
			public void invoke() {
				// create a new file opening dialog
				JFileChooser chooser = new JFileChooser("");
				chooser.setDialogType(JFileChooser.OPEN_DIALOG);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setFileFilter(new FileNameExtensionFilter(
						"Hyperworkflows (*.hwf)", "hwf"));
				chooser.setVisible(true);
				int result = chooser.showOpenDialog(null);

				// once file choice is approved, load the chosen file
				if (result == JFileChooser.APPROVE_OPTION) {
					File chosenFile = chooser.getSelectedFile();
					String filePath = chosenFile.getPath();
					MutableWorkflow<?> hwf;
					try {
						hwf = Serialization.load(app, filePath);
						new WorkflowEditorImpl(app, hwf, toolFactories);
					} catch (Exception e) {
						app.sendMessage(new ExceptionMessage(e));
					}
				}
			}
		}

		protected class SaveWorkflowAction implements Action {

			private final Model<?> model;

			public SaveWorkflowAction(Model<?> model) {
				this.model = model;
			}

			@Override
			public String getName() {
				return "Save Workflow";
			}

			@Override
			public void invoke() {
				// create a new file opening dialog
				@SuppressWarnings("serial")
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
						"Hyperworkflows (*.hwf)", "hwf"));
				chooser.setVisible(true);
				int result = chooser.showSaveDialog(null);

				// once file choice is approved, save the chosen file
				if (result == JFileChooser.APPROVE_OPTION) {
					File chosenFile = chooser.getSelectedFile();
					String filePath = chosenFile.getPath();
					try {
						Serialization.save(app, model.getRoot(), filePath);
					} catch (Exception e) {
						app.sendMessage(new ExceptionMessage(e));
					}
				}
			}
		}

	}
}
