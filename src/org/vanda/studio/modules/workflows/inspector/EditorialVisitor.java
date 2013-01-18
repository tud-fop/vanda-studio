package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.model.Model.SelectionVisitor;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

public final class EditorialVisitor implements SelectionVisitor {

	private final Application app;
	private final ElementEditorFactories eefs;
	private JComponent editor = null;

	public EditorialVisitor(ElementEditorFactories eefs, Application app) {
		this.app = app;
		this.eefs = eefs;
	}

	@Override
	public void visitWorkflow(MutableWorkflow wf) {
		editor = eefs.workflowFactories.createEditor(app, wf, wf);
	}

	@Override
	public void visitConnection(MutableWorkflow wf, ConnectionKey cc) {
		editor = eefs.connectionFactories.createEditor(app, wf, cc);
	}

	@Override
	public void visitJob(final MutableWorkflow wf, final Job j) {
		j.visit(new ElementVisitor() {

			@Override
			public void visitLiteral(Literal l) {
				editor = eefs.literalFactories
						.createEditor(app, wf, l);
			}

			@Override
			public void visitTool(Tool t) {
				editor = eefs.toolFactories.createEditor(app, wf, t);
			}

		});
	}

	public JComponent getEditor() {
		return editor;
	}

	@Override
	public void visitVariable(Location variable, MutableWorkflow wf) {
		editor = eefs.variableFactories.createEditor(app, wf, variable);
	}

}