package org.vanda.studio.modules.workflows.inspector;

import java.util.List;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Model.SelectionVisitor;
import org.vanda.studio.model.elements.Choice;
import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.OutputPort;
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
	public void visitWorkflow(List<Token> path, MutableWorkflow wf) {
		editor = eefs.workflowFactories.createEditor(app, path, null, wf);
	}

	@Override
	public void visitConnection(List<Token> path, Token address,
			MutableWorkflow wf, Connection cc) {
		editor = eefs.connectionFactories.createEditor(app, path, address, cc);
	}

	@Override
	public void visitJob(final List<Token> path, final Token address,
			final MutableWorkflow wf, final Job j) {
		j.getItem().visit(new RepositoryItemVisitor() {

			@Override
			public void visitChoice(Choice c) {
				editor = eefs.choiceFactories.createEditor(app, path, address,
						c);
			}

			@Override
			public void visitInputPort(InputPort i) {
				editor = eefs.inputPortFactories.createEditor(app, path,
						address, i);
			}

			@Override
			public void visitLinker(Linker l) {
				editor = eefs.linkerFactories.createEditor(app, path, address,
						l);
			}

			@Override
			public void visitLiteral(Literal l) {
				editor = eefs.literalFactories.createEditor(app, path, address,
						l);
			}

			@Override
			public void visitOutputPort(OutputPort o) {
				editor = eefs.outputPortFactories.createEditor(app, path,
						address, o);
			}

			@Override
			public void visitTool(Tool t) {
				editor = eefs.toolFactories.createEditor(app, path, address, t);
			}

		});
	}

	public JComponent getEditor() {
		return editor;
	}

	@Override
	public void visitVariable(List<Token> path, Token variable,
			MutableWorkflow wf) {
		editor = eefs.variableFactories.createEditor(app, path, variable, wf);
	}

}