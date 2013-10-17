package org.vanda.studio.modules.workflows.tools.semantic;

import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.KeyStroke;

import org.vanda.fragment.model.Generator;
import org.vanda.studio.app.Application;
import org.vanda.studio.modules.previews.WorkflowExecutionPreview;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.studio.modules.workflows.run.ExecutableWorkflowFactory;
import org.vanda.studio.modules.workflows.run.RunConfig;
import org.vanda.studio.modules.workflows.run.RunConfigEditor;
import org.vanda.studio.modules.workflows.run.RunConfigEditor.Runner;
import org.vanda.types.Types;
import org.vanda.util.Action;
import org.vanda.util.ExceptionMessage;
import org.vanda.util.Pair;
import org.vanda.view.View;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;
import org.vanda.workflows.serialization.Storer;

public class RunTool implements SemanticsToolFactory {
	private class Tool {
		/**
		 * Opens a dialog in which the setting for a RunConifg can be assigned. 
		 * On execution it creates the RunConfig and opens the ExecutionPerspetive.
		 * @author kgebhardt
		 *
		 */
		public final class RunAction implements Action, Runner {
			private JDialog f;

			@Override
			public String getName() {
				return "Open Execution Preview...";
			}

			@Override
			public void invoke() {
				boolean validWorkflow = true;
				try {
					synA.checkWorkflow();
				} catch (Exception e1) {
					validWorkflow = false;
					// app.sendMessage(new ExceptionMessage(e1));
				}
				validWorkflow &= semA.getDFA().isConnected()
						&& Types.canUnify(synA.getFragmentType(), prof.getRootType());
				f = new JDialog(wfe.getApplication().getWindowSystem().getMainWindow(), "Execute Workflow");
				RunConfigEditor rce = new RunConfigEditor(wfe.getView().getWorkflow(), wfe.getDatabase(),
						app.getRootDataSource(), app.getProperty("outputPath"), RunAction.this, validWorkflow);
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
				
				// TODO: probably obsolete, remove after testing
				// String id = generate();
				// if (id != null) {
				// serialize Workflow + Database
				Map<String, Integer> prioMapInst = new HashMap<String, Integer>();
				MutableWorkflow ewf = ExecutableWorkflowFactory.generateExecutableWorkflow(wfe.getView().getWorkflow(),
						wfe.getDatabase(), assingmentSelection, synA, semA, prioMap, prioMapInst);
				filePath += "/" + ewf.getName() + new Date().toString();
				RunConfig rc = new RunConfig(filePath, prioMapInst);
				try {
					new Storer().store(ewf, wfe.getDatabase(), filePath + ".xwf");
					new org.vanda.workflows.serialization.run.Storer().store(rc, filePath + ".run");
					// create WorkflowExecutionPreview from file
					new WorkflowExecutionPreview(app, prof).createPreview(filePath);
				} catch (Exception e) {
					wfe.getApplication().sendMessage(new ExceptionMessage(e));
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
			wfe.addAction(new RunAction(), "system-run", KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK), 4);
		}

		// TODO: probably obsolete, remove after some testing
		// private String generate() {
		// // try {
		// // synA.checkWorkflow();
		// // } catch (Exception e1) {
		// // app.sendMessage(new ExceptionMessage(e1));
		// // }
		// if (semA.getDFA().isConnected() &&
		// Types.canUnify(synA.getFragmentType(), prof.getRootType())) {
		// try {
		// return prof.generate(wfe.getView().getWorkflow(), synA, semA);
		// } catch (IOException e) {
		// app.sendMessage(new ExceptionMessage(e));
		// }
		// }
		// return null;
		// }
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
