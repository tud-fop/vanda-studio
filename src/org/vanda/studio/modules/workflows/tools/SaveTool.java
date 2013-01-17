package org.vanda.studio.modules.workflows.tools;

import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.workflows.hyper.Serialization;

public final class SaveTool implements ToolFactory {
	@Override
	public Object instantiate(WorkflowEditor wfe) {
		Action a = new SaveWorkflowAction(wfe);
		wfe.addAction(a,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		return a;
	}

	@Override
	public String getCategory() {
		return "Workflow Serialization";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "Permits saving the workflow";
	}

	@Override
	public String getId() {
		return "serialize-tool";
	}

	@Override
	public String getName() {
		return "Serialization tool";
	}

	@Override
	public String getVersion() {
		return "2012-12-12";
	}

	protected class SaveWorkflowAction implements Action {

		private final WorkflowEditor wfe;

		public SaveWorkflowAction(WorkflowEditor wfe) {
			this.wfe = wfe;
		}

		@Override
		public String getName() {
			return "Save Workflow...";
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
			String lastDir = wfe.getApplication().getProperty("lastDir");
			if (lastDir != null)
				chooser.setCurrentDirectory(new File(lastDir));
			chooser.setVisible(true);
			int result = chooser.showSaveDialog(null);

			// once file choice is approved, save the chosen file
			if (result == JFileChooser.APPROVE_OPTION) {
				File chosenFile = chooser.getSelectedFile();
				wfe.getApplication().setProperty("lastDir",
						chosenFile.getParentFile().getAbsolutePath());
				String filePath = chosenFile.getPath();
				try {
					Serialization ser = new Serialization(wfe.getApplication()
							.getToolMetaRepository().getRepository());
					ser.save(wfe.getModel().getRoot(), filePath);
				} catch (Exception e) {
					wfe.getApplication().sendMessage(new ExceptionMessage(e));
				}
			}
		}
	}

}