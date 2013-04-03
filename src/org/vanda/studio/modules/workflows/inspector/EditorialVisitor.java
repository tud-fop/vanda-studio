package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.model.WorkflowDecoration.SelectionVisitor;
import org.vanda.studio.modules.workflows.model.WorkflowDecoration.WorkflowSelection;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

public final class EditorialVisitor implements SelectionVisitor {

	private final ElementEditorFactories eefs;
	private AbstractEditorFactory editorFactory = null;

	public EditorialVisitor(ElementEditorFactories eefs) {
		this.eefs = eefs;
	}

	@Override
	public void visitWorkflow(final MutableWorkflow wf) {
		editorFactory = new AbstractEditorFactory() {
			@Override
			public JComponent createEditor(Application app) {
				return eefs.workflowFactories.createEditor(app, wf, wf);
			}
		};
	}

	@Override
	public void visitConnection(final MutableWorkflow wf, final ConnectionKey cc) {
		editorFactory = new AbstractEditorFactory() {
			@Override
			public JComponent createEditor(Application app) {
				return eefs.connectionFactories.createEditor(app, wf, cc);
			}
		};
	}

	@Override
	public void visitJob(final MutableWorkflow wf, final Job j) {
		j.visit(new ElementVisitor() {

			@Override
			public void visitLiteral(final Literal l) {
				editorFactory = new AbstractEditorFactory() {
					@Override
					public JComponent createEditor(Application app) {
						return eefs.literalFactories.createEditor(app, wf, l);
					}
				};
			}

			@Override
			public void visitTool(final Tool t) {
				editorFactory = new AbstractEditorFactory() {
					@Override
					public JComponent createEditor(Application app) {
						return eefs.toolFactories.createEditor(app, wf, t);
					}
				};
			}

		});
	}

	public AbstractEditorFactory getEditorFactory() {
		return editorFactory;
	}

	@Override
	public void visitVariable(final Location variable, final MutableWorkflow wf) {
		editorFactory = new AbstractEditorFactory() {
			@Override
			public JComponent createEditor(Application app) {
				return eefs.variableFactories.createEditor(app, wf, variable);
			}
		};
	}
	
	public static AbstractEditorFactory createAbstractFactory(ElementEditorFactories eefs, WorkflowSelection ws) {
		EditorialVisitor visitor = new EditorialVisitor(eefs);
		if (ws != null)
			ws.visit(visitor);
		return visitor.getEditorFactory();
	}

}