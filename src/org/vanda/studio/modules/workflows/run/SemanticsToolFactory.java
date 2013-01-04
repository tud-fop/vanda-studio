package org.vanda.studio.modules.workflows.run;

import org.vanda.fragment.model.Model;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.workflows.elements.RepositoryItem;

public interface SemanticsToolFactory extends RepositoryItem {
	Object instantiate(WorkflowEditor wfe, Model model);

}
