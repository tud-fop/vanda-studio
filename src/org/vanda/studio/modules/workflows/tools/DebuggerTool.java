package org.vanda.studio.modules.workflows.tools;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.vanda.studio.modules.workflows.model.Model;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Observer;
import org.vanda.workflows.elements.RepositoryItemVisitor;

public class DebuggerTool implements ToolFactory {

	private static final class Tool {
		private final WorkflowEditor wfe;
		private final Model m;
		private final JTextArea debugger;
		private final JScrollPane therealdebugger;

		public Tool(WorkflowEditor wfe) {
			this.wfe = wfe;
			this.m = wfe.getModel(); // XXX better not cache this
			debugger = new JTextArea();
			this.m.getWorkflowCheckObservable().addObserver(
					new Observer<Model>() {
						@Override
						public void notify(Model event) {
							update(event);
						}
					});
			therealdebugger = new JScrollPane(debugger);
			therealdebugger.setName("Debugger");
			this.wfe.addToolWindow(therealdebugger);
		}

		public void update(Model model) {
			StringBuilder sb = new StringBuilder();
			model.getFrozen().appendText(sb);
			if (!model.getFrozen().isSane()) {
				sb.append("Warning: Your workflow(s) are not executable!\n"
						+ "The most likely reason is that some input port "
						+ "is not connected.\n\n");
			}
			debugger.setText(sb.toString());
		}

	}

	@Override
	public Object instantiate(WorkflowEditor wfe) {
		return new Tool(wfe);
	}

	@Override
	public String getCategory() {
		return "Workflow Inspection";
	}

	@Override
	public String getContact() {
		return "Matthias.Buechse@tu-dresden.de";
	}

	@Override
	public String getDescription() {
		return "Shows the system of equations behind the workflow.";
	}

	@Override
	public String getId() {
		return "debugger-tool";
	}

	@Override
	public String getName() {
		return "Debugger Tool";
	}

	@Override
	public String getVersion() {
		return "2012-12-12";
	}

	@Override
	public void visit(RepositoryItemVisitor v) {
		
	}

}
