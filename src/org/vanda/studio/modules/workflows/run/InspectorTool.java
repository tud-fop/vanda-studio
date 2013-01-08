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
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.studio.modules.workflows.model.Model.SelectionVisitor;
import org.vanda.studio.modules.workflows.model.Model.WorkflowSelection;
import org.vanda.types.Type;
import org.vanda.util.Observer;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.hyper.Connection;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;

public class InspectorTool implements SemanticsToolFactory {

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

		private class InspectorialVisitor implements SelectionVisitor {

			String value = null;
			Type type = null;

			@Override
			public void visitWorkflow(MutableWorkflow wf) {
			}

			@Override
			public void visitConnection(Token address, MutableWorkflow wf,
					Connection cc) {
				visitVariable(wf.getVariable(address), wf);
			}

			@Override
			public void visitJob(Token address, MutableWorkflow wf, Job j) {
			}

			@Override
			public void visitVariable(Token variable, MutableWorkflow wf) {
				type = mm.getDataflowAnalysis().getWorkflow().getType(variable);
				value = mm.getDataflowAnalysis().getValue(variable);
				// XXX no support for nested workflows because wf is ignored
			}

		}

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
			wfe.getModel().getSelectionChangeObservable().addObserver(obs);
			wfe.getModel().getWorkflowObservable().addObserver(obs);
			wfe.getModel().getChildObservable().addObserver(obs);
			mm.getDfaChangedObservable().addObserver(obs);
			update();
		}

		public void update() {
			WorkflowSelection ws = wfe.getModel().getSelection();
			if (ws == null)
				ws = new WorkflowSelection(wfe.getModel().getRoot());
			// set inspector text
			String newvalue = null;
			JComponent newpreview = dummyPreview;
			Type type = null;
			// if (frozen != null && dfa != null) {
				InspectorialVisitor visitor = new InspectorialVisitor();
				if (ws != null) // <--- always true for now
					ws.visit(visitor);
				newvalue = visitor.value;
				type = visitor.type;
			// }
			if (newvalue != value) {
				value = newvalue;
				if (type != null && value != null) {
					fileName.setText(value + " :: " + type.toString());
					// create preview
					pf = wfe.getApplication().getPreviewFactory(type);
					if (pf != null)
						newpreview = pf.createPreview(wfe.getApplication()
								.findFile(value));
				} else
					fileName.setText("");
			} else
				newpreview = preview;
			if (newpreview != preview) {
				if (preview != null)
					contentPane.remove(preview);
				preview = newpreview;
				if (preview != null)
					contentPane.add(preview, gbc);
			}
		}
	}

	public InspectorTool() {
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, Model model) {
		return new Inspector(wfe, model);
	}

	@Override
	public String getCategory() {
		return "Workflow Inspection";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getId() {
		return "profile-inspector";
	}

	@Override
	public String getName() {
		return "Profile Semantics Inspector";
	}

	@Override
	public String getVersion() {
		return "2012-12-12";
	}

}
