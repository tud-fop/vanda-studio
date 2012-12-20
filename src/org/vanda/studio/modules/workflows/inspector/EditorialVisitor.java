package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Model.SelectionVisitor;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.util.TokenSource.Token;

final class EditorialVisitor implements SelectionVisitor {

	private final Application app;
	private final ElementEditorFactories eefs;
	private JComponent editor = null;

	public EditorialVisitor(ElementEditorFactories eefs, Application app) {
		this.app = app;
		this.eefs = eefs;
	}

	@Override
	public void visitWorkflow(MutableWorkflow wf) {
		editor = eefs.workflowFactories.createEditor(app, wf, null, wf);
	}

	@Override
	public void visitConnection(Token address, MutableWorkflow wf, Connection cc) {
		editor = eefs.connectionFactories.createEditor(app, wf, address, cc);
	}

	@Override
	public void visitJob(final Token address, final MutableWorkflow wf,
			final Job j) {
		j.getItem().visit(new RepositoryItemVisitor() {

			@Override
			public void visitLiteral(Literal l) {
				editor = eefs.literalFactories
						.createEditor(app, wf, address, l);
			}

			@Override
			public void visitTool(Tool t) {
				editor = eefs.toolFactories.createEditor(app, wf, address, t);
			}

		});
	}

	public JComponent getEditor() {
		return editor;
	}

	@Override
	public void visitVariable(Token variable, MutableWorkflow wf) {
		editor = eefs.variableFactories.createEditor(app, wf, variable, wf);
	}

}