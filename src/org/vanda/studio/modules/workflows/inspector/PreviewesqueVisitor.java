package org.vanda.studio.modules.workflows.inspector;

import javax.swing.JComponent;

import org.vanda.fragment.model.Model;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.model.WorkflowDecoration.SelectionVisitor;
import org.vanda.studio.modules.workflows.model.WorkflowDecoration.WorkflowSelection;
import org.vanda.types.Type;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

public class PreviewesqueVisitor implements SelectionVisitor {

	private final Model mm;
	private AbstractPreviewFactory apf;
	
	public PreviewesqueVisitor(Model mm) {
		this.mm = mm;
		apf = null;
	}
	
	public static AbstractPreviewFactory createPreviewFactory(Model mm, WorkflowSelection ws) {
		PreviewesqueVisitor visitor = new PreviewesqueVisitor(mm);
		if (ws != null)
			ws.visit(visitor);
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
		visitVariable(wf.getConnectionValue(cc), wf);
	}

	@Override
	public void visitJob(MutableWorkflow wf, Job j) {
	}

	@Override
	public void visitVariable(Location variable, MutableWorkflow wf) {
		// XXX no support for nested workflows because wf is ignored
		final Type type = mm.getType(variable);
		final String value = mm.getDataflowAnalysis().getValue(variable);
		apf = new AbstractPreviewFactory() {
			@Override
			public JComponent createPreview(Application app) {
				return app.getPreviewFactory(type).createPreview(app.findFile(value));
			}
		};
	}

}