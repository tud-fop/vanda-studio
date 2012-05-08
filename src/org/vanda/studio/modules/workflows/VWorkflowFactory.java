package org.vanda.studio.modules.workflows;

import java.io.File;

import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.ToolFactory;

public class VWorkflowFactory implements ToolFactory<VWorkflow> {

	@Override
	public VWorkflow createInstance(ModuleInstance<VWorkflow> mod, File f) {
		return new VWorkflowImpl(mod, f);
	}
}
