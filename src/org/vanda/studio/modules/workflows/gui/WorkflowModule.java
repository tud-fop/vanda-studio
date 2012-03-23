package org.vanda.studio.modules.workflows.gui;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.Action;
import org.vanda.studio.modules.common.Editor;
import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.SimpleModule;
import org.vanda.studio.modules.common.SimpleModuleInstance;
import org.vanda.studio.modules.common.VObjectFactory;

public class WorkflowModule implements SimpleModule<VWorkflow> {
	
	@Override
	public Editor<VWorkflow> createEditor(Application app) {
		return new WorkflowEditor(app);
	}
	
	@Override
	public VObjectFactory<VWorkflow> createFactory() {
		return new VWorkflowFactory();
	}
	
	@Override
	public ModuleInstance<VWorkflow> createInstance(Application app) {
		return new WorkflowModuleInstance(app, this);
	}
	
	@Override
	public String getExtension() {
		return ".workflow";
	}

	@Override
	public String getName() {
		return "Workflows"; // Module for Vanda Studio";
	}
	
	protected static class WorkflowModuleInstance
	extends SimpleModuleInstance<VWorkflow> {
		
		public WorkflowModuleInstance(Application a, SimpleModule<VWorkflow> m) {
			super(a, m);
			
			app.getWindowSystem().addAction(new NewWorkflowAction());
		}
		
		protected class NewWorkflowAction implements Action {
			@Override
			public String getName() {
				return "New Hyperworkflow";
			}
			
			@Override
			public void invoke() {
				// create term (file)
				VWorkflow t = factory.createInstance(WorkflowModuleInstance.this, null);
				// do something with the repository
				//repository.addItem(t); FIXME
				// open editor for term
				openEditor(t);
			}
		}
	}
}