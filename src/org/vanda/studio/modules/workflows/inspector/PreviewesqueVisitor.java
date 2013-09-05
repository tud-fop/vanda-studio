package org.vanda.studio.modules.workflows.inspector;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.vanda.studio.app.Application;
import org.vanda.types.Type;
import org.vanda.view.AbstractView;
import org.vanda.view.AbstractView.SelectionVisitor;
import org.vanda.view.View;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class PreviewesqueVisitor implements SelectionVisitor {

	private final SemanticAnalysis semA;
	private final SyntaxAnalysis synA;
	private AbstractPreviewFactory apf;

	public PreviewesqueVisitor(SemanticAnalysis semA, SyntaxAnalysis synA) {
		this.semA = semA;
		this.synA = synA;
		apf = null;
	}

	public static AbstractPreviewFactory createPreviewFactory(SemanticAnalysis semA, SyntaxAnalysis synA, View view) {
		PreviewesqueVisitor visitor = new PreviewesqueVisitor(semA, synA);
		// Show Workflow-Preview in case of multi-selection
		if (view.getCurrentSelection().size() > 1)
			view.getWorkflowView().visit(visitor, view);
		else
			for (AbstractView av : view.getCurrentSelection())
				av.visit(visitor, view);
		return visitor.getPreviewFactory();
	}

	public AbstractPreviewFactory getPreviewFactory() {
		return apf;
	}

	@Override
	public void visitWorkflow(MutableWorkflow wf) {
	}

	@Override
	public void visitConnection(MutableWorkflow wf, ConnectionKey cc) {
		visitVariable(MutableWorkflow.getConnectionValue(cc), wf);
	}

	@Override
	public void visitJob(MutableWorkflow wf, Job j) {
	}

	@Override
	public void visitVariable(Location variable, MutableWorkflow wf) {
		// XXX no support for nested workflows because wf is ignored
		final Type type = synA.getType(variable);
		final String value = semA.getDFA().getValue(variable);
		apf = new AbstractPreviewFactory() {
			@Override
			public JComponent createPreview(Application app) {
				return app.getPreviewFactory(type).createPreview(app.findFile(value));
			}

			@Override
			public JComponent createButtons(final Application app) {
				JPanel pan = new JPanel(new GridLayout(3,1));
				JButton bOpen = new JButton(new AbstractAction("edit") {
					private static final long serialVersionUID = 2959913172246062587L;

					@Override
					public void actionPerformed(ActionEvent e) {
						app.getPreviewFactory(type).openEditor(app.findFile(value));
					}
				});
				pan.add(bOpen);
				return pan;
			}
		};

	}

}