package org.vanda.studio.modules.workflows.impl;

import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.vanda.fragment.model.Generator;
import org.vanda.fragment.model.SemanticAnalysis;
import org.vanda.fragment.model.SyntaxAnalysis;
import org.vanda.presentationmodel.execution.PresentationModel;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.run2.Run;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Pair;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.TypeCheckingException;

import com.mxgraph.swing.mxGraphComponent;

public class WorkflowExecution extends DefaultWorkflowEditorImpl {

	private final class CancelAction implements Action {
		private final Run run;

		private CancelAction(Run run) {
			this.run = run;
		}

		@Override
		public String getName() {
			return "Cancel";
		}

		@Override
		public void invoke() {
			run.cancel();
			app.getWindowSystem().disableAction(this);
		}
	}

	private final class RunAction implements Action {

		@Override
		public String getName() {
			return "Run";
		}

		@Override
		public void invoke() {
			// TODO retrieve "id" from some .run file, no need to build
			// fragment again
			String id = generate();
			System.out.println(id);
			if (id != null) {
				System.out.println("invoked RunAction");
				Run run = new Run(app, pm.getView().getRunEventObserver(), id);
				run.run();
				addAction(new CancelAction(run),
						KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
				
				app.getWindowSystem().disableAction(this);
			}
		}
	}

	private final Generator prof;
	private final PresentationModel pm;

	public WorkflowExecution(Application app, Pair<MutableWorkflow, Database> phd, Generator prof)
			throws TypeCheckingException {
		super(app, phd);
		this.prof = prof;

		synA = new SyntaxAnalysis(phd.fst, true);
		semA = new SemanticAnalysis(synA, null, true);

		pm = new PresentationModel(view);

		// setup component design
		component = (mxGraphComponent) pm.getVisualization().getGraphComponent();
		configureComponent();
		component.setName(phd.fst.getName() + "Execution");
		
		addAction(new RunAction(), KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
	}

	private String generate() {
		try {
			return prof.generate(view.getWorkflow(), synA, semA);
		} catch (IOException e) {
			app.sendMessage(new ExceptionMessage(e));
		}
		return null;
	}

	public JComponent getComponent() {
		return component;
	}

}