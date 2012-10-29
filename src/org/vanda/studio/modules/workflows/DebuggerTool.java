package org.vanda.studio.modules.workflows;

import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.vanda.studio.app.ToolFactory;
import org.vanda.studio.app.WorkflowEditor;
import org.vanda.studio.model.Model;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.util.Observer;

public class DebuggerTool implements ToolFactory {

	private static final class Tool {
		private final WorkflowEditor wfe;
		private final Model m;
		private final JTextArea debugger;
		private final JScrollPane therealdebugger;

		public Tool(WorkflowEditor wfe, Model m) {
			this.wfe = wfe;
			this.m = m;
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
			sb.append("Instances\n");
			List<ImmutableWorkflow> iwfs = model.getUnfolded();
			for (ImmutableWorkflow i : iwfs) {
				sb.append("-------\n\n");
				i.appendText(sb);
				sb.append("\n");
			}
			debugger.setText(sb.toString());
		}

	}

	@Override
	public Object instantiate(WorkflowEditor wfe, Model m) {
		return new Tool(wfe, m);
	}

}
