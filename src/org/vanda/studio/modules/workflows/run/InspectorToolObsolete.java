package org.vanda.studio.modules.workflows.run;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.vanda.fragment.model.Model;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.modules.workflows.inspector.AbstractPreviewFactory;
import org.vanda.studio.modules.workflows.inspector.PreviewesqueVisitor;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.studio.modules.workflows.model.WorkflowDecoration.WorkflowSelection;
import org.vanda.util.Observer;

public class InspectorToolObsolete implements SemanticsToolFactory {
	
	public final class Inspector {
		private final WorkflowEditor wfe;
		private final Model mm;
		private final JPanel contentPane;
		private final JLabel fileName;
		private String value;
		private PreviewFactory pf;
		private GridBagConstraints gbc;
		private JLabel dummyPreview;
		private JComponent preview;

		public Inspector(WorkflowEditor wfe, Model mm) {
			this.wfe = wfe;
			this.mm = mm;
			fileName = new JLabel("Select a location or a connection.");
			contentPane = new JPanel(new GridBagLayout());
			@SuppressWarnings("serial")
			JButton bOpenEditor = new JButton(new AbstractAction("Editor") {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (pf != null && value != null)
						pf.openEditor(Inspector.this.wfe.getApplication()
								.findFile(value));
				}
			});

			dummyPreview = new JLabel(
					"<html><center>(select a location or a connection<br> for a quick preview)</center></html>",
					SwingConstants.CENTER);
			preview = dummyPreview;

			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			contentPane.add(fileName, gbc);

			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			contentPane.add(bOpenEditor, gbc);

			gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridheight = 1;
			gbc.gridwidth = 2;
			contentPane.add(preview, gbc);

			contentPane.setName("Semantics Inspector");
			value = null;
			pf = null;
			this.wfe.addToolWindow(contentPane);
			Observer<Object> obs = new Observer<Object>() {
				@Override
				public void notify(Object event) {
					update();
				}
			};
			wfe.getWorkflowDecoration().getSelectionChangeObservable().addObserver(obs);
			// wfe.getModel().getWorkflowObservable().addObserver(obs);
			// wfe.getModel().getChildObservable().addObserver(obs);
			mm.getDfaChangedObservable().addObserver(obs);
			update();
		}
		
		public void setPreview(AbstractPreviewFactory previewFactory) {
			if (preview != null) {
				contentPane.remove(preview);
				preview = null;
			}
			if (previewFactory != null) {
				preview = previewFactory.createPreview(wfe.getApplication());
				contentPane.add(preview, gbc);
			}
		}

		public void update() {
			WorkflowSelection ws = wfe.getWorkflowDecoration().getSelection();
			if (ws == null)
				ws = new WorkflowSelection(wfe.getWorkflowDecoration().getRoot());
			setPreview(PreviewesqueVisitor.createPreviewFactory(mm, ws));
		}
	}

	public InspectorToolObsolete() {
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, Model model) {
		return new Inspector(wfe, model);
	}

}
