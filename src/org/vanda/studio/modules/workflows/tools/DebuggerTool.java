package org.vanda.studio.modules.workflows.tools;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Observer;
import org.vanda.view.View;
import org.vanda.view.Views.*;

public class DebuggerTool implements ToolFactory {

	private static final class Tool {
		private final WorkflowEditor wfe;
//		private final WorkflowDecoration m;
		private final JTextArea debugger;
		private final JScrollPane therealdebugger;
		private final View view;

		public Tool(WorkflowEditor wfe, View view) {
			this.wfe = wfe;
			this.view = view;
			//this.m = wfe.getWorkflowDecoration(); // XXX better not cache this
			
			debugger = new JTextArea();
			this.view.getObservable().addObserver(
			//this.m.getWorkflowCheckObservable().addObserver(
					new Observer<ViewEvent<View>>() {

						@Override
						public void notify(ViewEvent<View> event) {
							// TODO Auto-generated method stub
							
						}
					});
			therealdebugger = new JScrollPane(debugger);
			therealdebugger.setName("Debugger");
			// FIXME this.wfe.addToolWindow(therealdebugger);
		}

//		public void update(WorkflowDecoration model) {
//			StringBuilder sb = new StringBuilder();
//			model.getFrozen().appendText(sb);
//			if (!model.getFrozen().isSane()) {
//				sb.append("Warning: Your workflow(s) are not executable!\n"
//						+ "The most likely reason is that some input port "
//						+ "is not connected.\n\n");
//			}
//			debugger.setText(sb.toString());
//		}

	}

	@Override
	public void instantiate(WorkflowEditor wfe) {
		new Tool(wfe, wfe.getView());
	}

}
