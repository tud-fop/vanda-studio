package org.vanda.studio.modules.profile.gui;

import org.vanda.studio.app.WorkflowEditor;
import org.vanda.studio.model.elements.RepositoryItem;
import org.vanda.studio.modules.profile.model.Model;

public interface SemanticsToolFactory extends RepositoryItem {
	Object instantiate(WorkflowEditor wfe, Model model);

}
