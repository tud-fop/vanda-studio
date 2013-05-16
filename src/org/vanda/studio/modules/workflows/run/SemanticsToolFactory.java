package org.vanda.studio.modules.workflows.run;

import org.vanda.fragment.model.Model;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.view.View;

public interface SemanticsToolFactory {
	Object instantiate(WorkflowEditor wfe, Model model, View view);

}
