package org.vanda.studio.modules.workflows.run;

import java.util.Collection;

import org.vanda.fragment.model.Model;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.CompositeRepository;
import org.vanda.util.MetaRepository;
import org.vanda.util.Repository;
import org.vanda.view.View;

public class SemanticsTool implements ToolFactory {
	
	private final static class Tool {
		
		Model model;
		View view;

		public Tool(WorkflowEditor wfe, Collection<SemanticsToolFactory> stfs) {
			model = new Model(wfe.getModel());
			view  = wfe.getView();
			for (SemanticsToolFactory stf : stfs)
				stf.instantiate(wfe, model, view);
		}
		
	}
	
	private final CompositeRepository<SemanticsToolFactory> repository;
	
	public SemanticsTool() {
		repository = new CompositeRepository<SemanticsToolFactory>();
	}

	public SemanticsTool(Repository<SemanticsToolFactory> srep) {
		repository = new CompositeRepository<SemanticsToolFactory>();
		repository.addRepository(srep);
	}

	@Override
	public String getCategory() {
		return "Semantics Tool Collections";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		// TODO compile description of SemanticsToolFactories in the repository
		return "";
	}

	@Override
	public String getId() {
		return "profile-semantics-tools";
	}

	@Override
	public String getName() {
		return "Profile Semantics Tool Collection";
	}
	
	public MetaRepository<SemanticsToolFactory> getSemanticsToolFactoryMetaRepository() {
		return repository;
	}

	@Override
	public String getVersion() {
		return "2012-12-20";
	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new Tool(wfe, repository.getItems());
	}

}
