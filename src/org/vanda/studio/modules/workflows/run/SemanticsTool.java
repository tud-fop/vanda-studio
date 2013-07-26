package org.vanda.studio.modules.workflows.run;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

//import org.vanda.fragment.model.Model;
import org.vanda.fragment.model.SemanticAnalysis;
import org.vanda.fragment.model.SyntaxAnalysis;

import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Observer;
import org.vanda.view.View;

public class SemanticsTool implements ToolFactory {

	private final static class Tool {
		private final class SemanticUpdater {
			private final SemanticAnalysis semA;

			public SemanticUpdater(SyntaxAnalysis synA, SemanticAnalysis semA) {
				synA.getSyntaxChangedObservable().addObserver(new Observer<SyntaxAnalysis>() {

					@Override
					public void notify(SyntaxAnalysis event) {
						update(event);
					}
				});
				this.semA = semA;
			}

			public void update(SyntaxAnalysis synA) {
				semA.updateDFA(synA);

			}
		}

		// Model model;
		SemanticAnalysis semA;
		SyntaxAnalysis synA;
		View view;
		SemanticUpdater semUp;

		public Tool(WorkflowEditor wfe, Collection<SemanticsToolFactory> stfs) {

			view = wfe.getView();
			// model = new Model(view, wfe.getDatabase());
			synA = wfe.getSyntaxAnalysis();
			semA = wfe.getSemanticAnalysis();
			semUp = new SemanticUpdater(synA, semA);

			for (SemanticsToolFactory stf : stfs)
				// stf.instantiate(wfe, model, view);
				stf.instantiate(wfe, synA, semA, view);

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
