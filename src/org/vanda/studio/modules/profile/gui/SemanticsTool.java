package org.vanda.studio.modules.profile.gui;

import java.util.Collection;

import org.vanda.studio.app.MetaRepository;
import org.vanda.studio.app.ToolFactory;
import org.vanda.studio.app.WorkflowEditor;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.modules.common.CompositeRepository;
import org.vanda.studio.modules.profile.model.Model;

public class SemanticsTool implements ToolFactory {
	
	private final static class Tool {
		
		Model model;

		public Tool(WorkflowEditor wfe, Collection<SemanticsToolFactory> stfs) {
			model = new Model(wfe.getModel());
			for (SemanticsToolFactory stf : stfs)
				stf.instantiate(wfe, model);
		}
		
	}
	
	private final CompositeRepository<SemanticsToolFactory> repository;
	
	public SemanticsTool() {
		repository = new CompositeRepository<SemanticsToolFactory>();
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
	public void visit(RepositoryItemVisitor v) {

	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new Tool(wfe, repository.getItems());
	}

}
