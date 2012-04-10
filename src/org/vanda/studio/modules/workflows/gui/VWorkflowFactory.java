package org.vanda.studio.modules.workflows.gui;

import java.io.File;

import org.vanda.studio.modules.common.ModuleInstance;
import org.vanda.studio.modules.common.VObjectFactory;

public class VWorkflowFactory implements VObjectFactory<VWorkflow> {

	@Override
	public VWorkflow createInstance(ModuleInstance<VWorkflow> mod, File f) {
		return new VWorkflowImpl(mod, f);
	}
}
