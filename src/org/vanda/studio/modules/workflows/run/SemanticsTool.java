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
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;

public class SemanticsTool implements ToolFactory {

	private final static class Tool {
		/**
		 * Updates SemanticAnalysis if SyntaxAnalysis or Data changed
		 * @author kgebhardt
		 *
		 */
		private final class SemanticUpdater implements DatabaseListener<Database>, Observer<DatabaseEvent<Database>> {
			
			private final SemanticAnalysis semA;
			private final SyntaxAnalysis synA;
			
			public SemanticUpdater(SyntaxAnalysis synA, SemanticAnalysis semA, Database db) {
				synA.getSyntaxChangedObservable().addObserver(new Observer<SyntaxAnalysis>() {

					@Override
					public void notify(SyntaxAnalysis event) {
						update(event);
					}
				});
				
				db.getObservable().addObserver(this);

				this.semA = semA;
				this.synA = synA;
			}

			public void update(SyntaxAnalysis synA) {
				semA.updateDFA(synA);
			}

			@Override
			public void cursorChange(Database d) {
				update(synA);
			}

			@Override
			public void dataChange(Database d, Object key) {
				update(synA);				
			}

			@Override
			public void notify(DatabaseEvent<Database> event) {
				event.doNotify(this);
			}

			@Override
			public void nameChange(Database d) {
				// do nothing
			}
		}

		// Model model;
		private SemanticAnalysis semA;
		private SyntaxAnalysis synA;
		private View view;
		private SemanticUpdater semUp;

		public Tool(WorkflowEditor wfe, Collection<SemanticsToolFactory> stfs) {

			view = wfe.getView();
			// model = new Model(view, wfe.getDatabase());
			synA = wfe.getSyntaxAnalysis();
			semA = wfe.getSemanticAnalysis();
			semUp = new SemanticUpdater(synA, semA, wfe.getDatabase());
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
