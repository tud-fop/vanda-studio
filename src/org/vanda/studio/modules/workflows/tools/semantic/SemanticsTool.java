package org.vanda.studio.modules.workflows.tools.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;




//import org.vanda.fragment.model.Model;

import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Observer;
import org.vanda.view.View;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.data.Databases.DatabaseEvent;
import org.vanda.workflows.data.Databases.DatabaseListener;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class SemanticsTool implements ToolFactory {

	private final static class Tool {
		/**
		 * Updates SemanticAnalysis if SyntaxAnalysis or Database changed
		 * @author kgebhardt
		 *
		 */
		private final class SemanticUpdater implements DatabaseListener<Database>, Observer<DatabaseEvent<Database>> {
			
			private final SemanticAnalysis semA;
			private final SyntaxAnalysis synA;
			private final Observer<SyntaxAnalysis> syntaxObserver;
			
			public SemanticUpdater(SyntaxAnalysis synA, SemanticAnalysis semA, Database db) {
				syntaxObserver = new Observer<SyntaxAnalysis>() {

					@Override
					public void notify(SyntaxAnalysis event) {
						update(event);
					}
				};
				synA.getSyntaxChangedObservable().addObserver(syntaxObserver);
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

		private final SemanticAnalysis semA;
		private final SyntaxAnalysis synA;
		private final View view;
		// Member variable needed to keep a reference to Observer
		@SuppressWarnings("unused")
		private final SemanticUpdater semUp;
		private final Collection<Object> tools;

		public Tool(WorkflowEditor wfe, Collection<SemanticsToolFactory> stfs) {

			view = wfe.getView();
			// model = new Model(view, wfe.getDatabase());
			synA = wfe.getSyntaxAnalysis();
			semA = wfe.getSemanticAnalysis();
			semUp = new SemanticUpdater(synA, semA, wfe.getDatabase());
			tools = new ArrayList<Object>();
			for (SemanticsToolFactory stf : stfs)
				// stf.instantiate(wfe, model, view);
				tools.add(stf.instantiate(wfe, synA, semA, view));

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

	private Tool tool = null;
	
	@Override
	public void instantiate(WorkflowEditor wfe) {
		tool = new Tool(wfe, repository);
	}
	
	public Tool getTool() {
		return tool;
	}

}
