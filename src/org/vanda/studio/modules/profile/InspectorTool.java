package org.vanda.studio.modules.profile;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.app.ToolFactory;
import org.vanda.studio.app.WorkflowEditor;
import org.vanda.studio.model.Model;
import org.vanda.studio.model.Model.SelectionVisitor;
import org.vanda.studio.model.Model.WorkflowSelection;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.profile.model.Profiles;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource.Token;

public class InspectorTool implements ToolFactory {

	public final class Inspector {
		private final WorkflowEditor wfe;
		private final Model m;
		private final JPanel contentPane;
		private final JLabel fileName;
		private final JScrollPane therealinspector;
		private ImmutableWorkflow frozen;
		private DataflowAnalysis dfa;
		private String value;
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
				type = frozen.getType(variable);
				value = dfa.getValue(variable);
				// XXX no support for nested workflows because wf is ignored
			}

		}

		Action aOpenEditor = new AbstractAction("Editor") {

			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							System.out.println("xdg-open "
									+ ProfileImpl.findFile(
											wfe.getApplication(), value));
							Runtime.getRuntime()
									.exec("xdg-open "
											+ ProfileImpl.findFile(
													wfe.getApplication(), value));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});

				t.start();
			}
		};

		public Inspector(WorkflowEditor wfe, Model m) {
			this.wfe = wfe;
			this.m = m;
			fileName = new JLabel("Select a location or a connection.");
			therealinspector = new JScrollPane();
			contentPane = new JPanel(new GridBagLayout());
			JButton bOpenEditor = new JButton(aOpenEditor);

			GridBagConstraints gbc = new GridBagConstraints();

			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			contentPane.add(fileName, gbc);

			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			contentPane.add(bOpenEditor, gbc);

			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.weighty = 1;
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridheight = 1;
			gbc.gridwidth = 2;
			contentPane.add(therealinspector, gbc);

			contentPane.setName("Semantics Inspector");
			frozen = null;
			dfa = null;
			value = null;
			preview = null;
			this.wfe.addToolWindow(contentPane);
			Observer<Object> obs = new Observer<Object>() {
				@Override
				public void notify(Object event) {
					update();
				}
			};
			Observer<Object> obs2 = new Observer<Object>() {
				@Override
				public void notify(Object event) {
					frozen = Inspector.this.m.getFrozen();
					if (frozen.isSane()) {
						List<ImmutableWorkflow> unfolded = Inspector.this.m
								.getUnfolded();
						if (unfolded != null && unfolded.size() != 0) {
							// XXX here I only support looking at the first
							// instance because the value of instances is not
							// yet clear anyway
							ImmutableWorkflow frozen = unfolded.get(0);
							dfa = new DataflowAnalysis(frozen);
							dfa.doIt(null, profiles);
						}
					}

					update();
				}
			};
			this.m.getSelectionChangeObservable().addObserver(obs);
			this.m.getWorkflowCheckObservable().addObserver(obs2);
			this.m.getWorkflowObservable().addObserver(obs);
			this.m.getChildObservable().addObserver(obs);
			update();
		}

		public void update() {
			WorkflowSelection ws = m.getSelection();
			if (ws == null)
				ws = new WorkflowSelection(m.getRoot());
			// set inspector text
			String newvalue = null;
			JComponent newpreview = null;
			Type type = null;
			if (frozen != null && dfa != null) {
				InspectorialVisitor visitor = new InspectorialVisitor();
				if (ws != null) // <--- always true for now
					ws.visit(visitor);
				newvalue = visitor.value;
				type = visitor.type;
			}
			if (newvalue != value) {
				value = newvalue;
				if (type != null && value != null) {
					fileName.setText(value + " :: " + type.toString());
					// create preview
					PreviewFactory pf = wfe.getApplication().getPreviewFactory(
							type);
					if (pf != null)
						newpreview = pf.createPreview(ProfileImpl.findFile(
								wfe.getApplication(), value));
				} else
					fileName.setText("");
			} else
				newpreview = preview;
			if (newpreview != preview) {
				preview = newpreview;
				therealinspector.setViewportView(null);
				if (preview != null) {
					therealinspector.setViewportView(preview);
					therealinspector.revalidate();
					preview.revalidate();
					contentPane.revalidate();
				}
			}
		}
	}

	private final Profiles profiles;

	public InspectorTool(Profiles profiles) {
		this.profiles = profiles;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, Model m) {
		return new Inspector(wfe, m);
	}

}
