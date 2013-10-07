package org.vanda.studio.modules.workflows.inspector;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.vanda.studio.app.Application;
import org.vanda.types.CompositeType;
import org.vanda.types.Type;
import org.vanda.view.View;
import org.vanda.view.Views.*;
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
		List<SelectionObject> sos = view.getCurrentSelection();
		if (sos.size() > 1)
			visitor.visitWorkflow(view.getWorkflow());
		else
			for (SelectionObject so : sos)
				so.visit(visitor, view.getWorkflow());
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
		visitVariable(wf, MutableWorkflow.getConnectionValue(cc));
	}

	@Override
	public void visitJob(MutableWorkflow wf, final Job j) {
		apf = new AbstractPreviewFactory() {
			
			@Override
			public JComponent createPreview(Application app) {
				String log = app.findFile(semA.getDFA().getJobSpec(j) + "/log");
				return app.getPreviewFactory(new CompositeType("log")).createPreview(log);
			}
			
			@Override
			public JComponent createButtons(Application app) {
				return new JPanel();
			}
		};
	}

	@Override
	public void visitVariable(MutableWorkflow wf, Location variable) {
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