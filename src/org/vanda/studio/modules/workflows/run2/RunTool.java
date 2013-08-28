package org.vanda.studio.modules.workflows.run2;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;
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
import org.vanda.studio.modules.workflows.run2.RunConfigEditor.Runner;
import org.vanda.types.Types;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Pair;
import org.vanda.view.View;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.serialization.Storer;

public class RunTool implements SemanticsToolFactory {
	private class Tool {
		public final class RunAction implements Action, Runner {
			private JDialog f;

			@Override
			public String getName() {
				return "Run";
			}

			@Override
			public void invoke() {
				f = new JDialog(wfe.getApplication().getWindowSystem().getMainWindow(), "Execute Workflow");
				RunConfigEditor rce = new RunConfigEditor(wfe.getView().getWorkflow().getChildren(), wfe.getDatabase(),
						app.getProperty("outputPath"), RunAction.this);
				f.setContentPane(rce.getComponent());
				f.setAlwaysOnTop(true);
				f.setAutoRequestFocus(true);
				f.setModal(true);
				f.pack();
				f.setLocationRelativeTo(app.getWindowSystem().getMainWindow());
				f.setVisible(true);

			}

			public void evokeExecution(List<Integer> assingmentSelection, String filePath,
					Map<Pair<Job, Integer>, Integer> prioMap) {
				f.dispose();
				String id = generate();
				if (id != null) {
					// serialize Workflow + Database
					Map<String, Integer> prioMapInst = new HashMap<String, Integer>();
					MutableWorkflow ewf = ExecutableWorkflowFactory.generateExecutableWorkflow(wfe.getView()
							.getWorkflow(), wfe.getDatabase(), assingmentSelection, synA, semA, prioMap, prioMapInst);
					filePath += "/" + ewf.getName() + new Date().toString();
					RunConfig rc = new RunConfig(filePath, prioMapInst);
					try {
						new Storer().store(ewf, wfe.getDatabase(), filePath + ".xwf");
						new org.vanda.workflows.serialization.run.Storer().store(rc, filePath + ".run");
					} catch (Exception e) {
						wfe.getApplication().sendMessage(new ExceptionMessage(e));
					}

					// create WorkflowExecutionPreview from file
					JComponent executionPreview = new WorkflowExecutionPreview(app, prof).createPreview(filePath);
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
			wfe.addAction(new RunAction(), KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK), 3);
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
