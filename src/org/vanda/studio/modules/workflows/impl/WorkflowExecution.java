package org.vanda.studio.modules.workflows.impl;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.vanda.execution.model.Runables.RunEvent;
import org.vanda.execution.model.Runables.RunEventListener;
import org.vanda.fragment.model.Generator;
import org.vanda.fragment.model.SemanticAnalysisEx;
import org.vanda.fragment.model.SyntaxAnalysis;
import org.vanda.presentationmodel.execution.PresentationModel;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.run.InspectorTool;
import org.vanda.studio.modules.workflows.run.SemanticsTool;
import org.vanda.studio.modules.workflows.run.SemanticsToolFactory;
import org.vanda.studio.modules.workflows.run2.Run;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Observer;
import org.vanda.util.Pair;
import org.vanda.workflows.data.Database;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.TypeCheckingException;

import com.mxgraph.swing.mxGraphComponent;

/**
 * execution environment for experiments
 * 
 * @author kgebhardt
 * 
 */
public class WorkflowExecution extends DefaultWorkflowEditorImpl {

	private final class CancelAction implements Action {
		private Run run;

		@Override
		public String getName() {
			return "Cancel";
		}

		@Override
		public void invoke() {
			run.cancel();
			disable();
		}

		public void disable() {
			app.getWindowSystem().disableAction(component, this);
		}

		public void enable(Run run) {
			this.run = run;
			app.getWindowSystem().enableAction(component, this);
			run.getObserver().addObserver(new Observer<RunEvent>() {

				@Override
				public void notify(RunEvent event) {
					event.doNotify(new RunEventListener() {

						@Override
						public void runStarted(String _) {
						}

						@Override
						public void runFinished(String id) {
							if (CancelAction.this.run.getId().equals(id))
								disable();
						}

						@Override
						public void runCancelled(String _) {
						}

						@Override
						public void cancelledAll() {
							disable();
						}

						@Override
						public void progressUpdate(String _, int progress) {
						}
					});
				}

			});
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
			if (id != null) {
			// FIXME	System.out.println("invoked RunAction");
				Run run = new Run(app, pm.getView().getRunEventObserver(), id);
				run.run();
				cancel.enable(run);
				app.getWindowSystem().disableAction(component, this);
			}
		}
	}

	private final Generator prof;
	private final PresentationModel pm;
	private final CancelAction cancel;

	public WorkflowExecution(Application app, Pair<MutableWorkflow, Database> phd, Generator prof)
			throws TypeCheckingException {
		super(app, phd);
		this.prof = prof;

		synA = new SyntaxAnalysis(phd.fst, true);
		semA = new SemanticAnalysisEx(synA);

		pm = new PresentationModel(view);
		cancel = new CancelAction();

		// setup component design
		component = (mxGraphComponent) pm.getVisualization().getGraphComponent();
		component = new MyMxGraphComponent(pm.getVisualization().getGraph());
		component.setConnectable(false);
		component.setDragEnabled(false);
		configureComponent();
		component.setName(phd.fst.getName() + "Execution");

		setupOutline();

		// add inspector
		ElementEditorFactories eefs = new ElementEditorFactories();
		LinkedList<SemanticsToolFactory> srep = new LinkedList<SemanticsToolFactory>();
		srep.add(new InspectorTool(eefs));
		new SemanticsTool(srep).instantiate(this);

		// add Menu-Actions
		addAction(new RunAction(), KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		addAction(cancel, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
		cancel.disable();
		addAction(new ResetZoomAction(), KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK));
		addAction(new CloseWorkflowAction(), KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_MASK));
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