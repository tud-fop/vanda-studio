package org.vanda.studio.modules.previews;


import javax.swing.JComponent;

import org.vanda.fragment.model.Generator;
import org.vanda.studio.app.Application;
import org.vanda.studio.app.PreviewFactory;
import org.vanda.studio.modules.workflows.impl.WorkflowExecution;
import org.vanda.studio.modules.workflows.run.RunConfig;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Pair;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.serialization.Loader;


public class WorkflowExecutionPreview implements PreviewFactory {
	final Application app;
	final Generator prof;

	public WorkflowExecutionPreview(Application app, Generator prof) {
		this.app = app;
		this.prof = prof;
	}

	@Override
	public JComponent createPreview(String filePath) {
		Pair<MutableWorkflow, Database> phd;
		RunConfig rc;
		try {
			phd = new Loader(app.getToolMetaRepository().getRepository()).load(filePath+".xwf");
			rc = new org.vanda.workflows.serialization.run.Loader().load(filePath+".run");
			WorkflowExecution wfe = new WorkflowExecution(app, phd, prof, rc);
			return wfe.getComponent();
		} catch (Exception e) {
			app.sendMessage(new ExceptionMessage(e));
			return null;
		}
	}

	@Override
	public JComponent createSmallPreview(String absolutePath) {
		return createPreview(absolutePath);
	}

	@Override
	public void openEditor(String value) {
		// do nothing
	}

}
