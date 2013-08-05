package org.vanda.studio.modules.workflows.run2;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.vanda.execution.model.ExecutableWorkflowFactory;
import org.vanda.fragment.model.Generator;
//import org.vanda.fragment.model.Model;
import org.vanda.fragment.model.SemanticAnalysis;
import org.vanda.fragment.model.SyntaxAnalysis;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.previews.WorkflowExecutionPreview;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.studio.modules.workflows.run.SemanticsToolFactory;
import org.vanda.studio.modules.workflows.run2.Runs.ExecutionLogic;
import org.vanda.types.Types;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.view.View;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.serialization.Storer;

public class RunTool implements SemanticsToolFactory {
	private class Tool  {
		public final class RunAction implements Action , ExecutionLogic {
			private JFrame f;
			@Override
			public String getName() {
				return "Run2";
			}

			@Override
			public void invoke() {
				f = new JFrame("Execute Workflow");
				RunConfigEditor rce = new RunConfigEditor(wfe.getDatabase(), "/tmp/", RunAction.this);
				f.setContentPane(rce.getComponent());
				f.setVisible(true);
				f.setSize(f.getPreferredSize());
			}

			public void evokeExecution(List<Integer> assingmentSelection, String filePath) {
				f.dispose();
				String id = generate();
				if (id != null) {
					// serialize Workflow + Database
					MutableWorkflow ewf = ExecutableWorkflowFactory.generateExecutableWorkflow(wfe.getView()
							.getWorkflow(), wfe.getDatabase(), assingmentSelection, synA, semA);
					// TODO use generic path!!
					// String filePath = "/tmp/executionTest";
					filePath += "/" + ewf.getName() + new Date().toString();
					try {
						new Storer().store(ewf, wfe.getDatabase(), filePath);
					} catch (Exception e) {
						wfe.getApplication().sendMessage(new ExceptionMessage(e));
					}

					// create WorkflowExecutionPreview from file
					JComponent executionPreview = new WorkflowExecutionPreview(app, prof).createPreview(filePath);

					if (executionPreview != null) {
						executionPreview.setName(id + " Run");

						// create tab with WorkflowExecutionPreview
						app.getWindowSystem().addContentWindow(null, executionPreview, null);
						app.getWindowSystem().focusContentWindow(executionPreview);

						executionPreview.requestFocusInWindow();
					}

				}
			}

		}

		private Application app;
		private WorkflowEditor wfe;
		private SemanticAnalysis semA;
		private SyntaxAnalysis synA;

		public Tool(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA) {
			this.wfe = wfe;
			this.synA = synA;
			this.semA = semA;
			app = wfe.getApplication();
			wfe.addAction(new RunAction(), KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
		}

		private String generate() {
			try {
				synA.checkWorkflow();
			} catch (Exception e1) {
				app.sendMessage(new ExceptionMessage(e1));
			}
			if (semA.getDFA().isConnected() && Types.canUnify(synA.getFragmentType(), prof.getRootType())) {
				try {
					return prof.generate(wfe.getView().getWorkflow(), synA, semA);
				} catch (IOException e) {
					app.sendMessage(new ExceptionMessage(e));
				}
			}
			return null;
		}
	}

	private final Generator prof;

	public RunTool(Generator prof) {
		this.prof = prof;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA, View view) {
		return new Tool(wfe, synA, semA);
	}

}
