package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;


import org.vanda.view.AbstractView.SelectionVisitor;
import org.vanda.view.AbstractView;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
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
	public EditorialVisitor(ElementEditorFactories eefs, View view) {
		this.eefs = eefs;
	}

	@Override
	public void visitWorkflow(final MutableWorkflow wf) {
		editorFactory = new AbstractEditorFactory() {
			@Override
			public JComponent createEditor(Database d) {
				return eefs.workflowFactories.createEditor(d, wf, wf);
			}
		};
	}

	@Override
	public void visitConnection(final MutableWorkflow wf, final ConnectionKey cc) {
		editorFactory = new AbstractEditorFactory() {
			@Override
			public JComponent createEditor(Database d) {
				return eefs.connectionFactories.createEditor(d, wf, cc);
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
					public JComponent createEditor(Database d) {
						return eefs.literalFactories.createEditor(d, wf, l);
					}
				};
			}

			@Override
			public void visitTool(final Tool t) {
				editorFactory = new AbstractEditorFactory() {
					@Override
					public JComponent createEditor(Database d) {
						return eefs.toolFactories.createEditor(d, wf, t);
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
			public JComponent createEditor(Database d) {
				return eefs.variableFactories.createEditor(d, wf, variable);
			}
		};
	}
	
	public static AbstractEditorFactory createAbstractFactory(ElementEditorFactories eefs, View view) {
		EditorialVisitor visitor = new EditorialVisitor(eefs, view);
		for (AbstractView av : view.getCurrentSelection()) 
			av.visit(visitor, view);
		return visitor.getEditorFactory();
	}


}