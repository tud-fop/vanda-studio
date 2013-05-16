package org.vanda.studio.modules.workflows.run;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.vanda.fragment.model.Model;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.view.View;

public class SemanticsTool implements ToolFactory {
	
	private final static class Tool {
		
		Model model;
		View view;

		public Tool(WorkflowEditor wfe, Collection<SemanticsToolFactory> stfs) {

			view  = wfe.getView();
			model = new Model(wfe.getWorkflowDecoration(), wfe.getDatabase());
			for (SemanticsToolFactory stf : stfs)
				stf.instantiate(wfe, model, view);
		}
		
	}
	
	private final LinkedList<SemanticsToolFactory> repository;
	
	public SemanticsTool() {
		repository = new LinkedList<SemanticsToolFactory>();
	}

	public SemanticsTool(List<SemanticsToolFactory> srep) {
		repository = new LinkedList<SemanticsToolFactory>(srep);
	}

	public LinkedList<SemanticsToolFactory> getSemanticsToolFactoryMetaRepository() {
		return repository;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new Tool(wfe, repository);
	}

}
