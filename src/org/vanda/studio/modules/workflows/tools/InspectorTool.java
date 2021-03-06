//package org.vanda.studio.modules.workflows.tools;
//
//import java.awt.BorderLayout;
//import java.util.List;
//
//import javax.swing.JComponent;
//import javax.swing.JEditorPane;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//
//import org.vanda.studio.modules.workflows.inspector.EditorialVisitor;
//import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
//import org.vanda.studio.modules.workflows.inspector.InspectorialVisitor;
//import org.vanda.studio.modules.workflows.model.WorkflowDecoration;
//import org.vanda.studio.modules.workflows.model.ToolFactory;
//import org.vanda.studio.modules.workflows.model.WorkflowEditor;
//import org.vanda.util.Observer;
//import org.vanda.view.AbstractView;
//import org.vanda.view.View;
//
//public class InspectorTool implements ToolFactory {
//
//	private final ElementEditorFactories eefs;
//
//	public final class Inspector {
//		private final WorkflowEditor wfe;
//		private final WorkflowDecoration m;
//		private final JPanel contentPane;
//		private final JEditorPane inspector;
//		private final JScrollPane therealinspector;
//		private JComponent editor;
////		private WorkflowSelection 
//		private List<AbstractView> ws;
//		private final View view;
//
//		public Inspector(WorkflowEditor wfe) {
//			this.wfe = wfe;
//			this.m = wfe.getWorkflowDecoration(); // XXX better not cache this
//			this.view = wfe.getView();
//			ws = null;
//			inspector = new JEditorPane("text/html", "");
//			inspector.setEditable(false);
//			therealinspector = new JScrollPane(inspector);
//			contentPane = new JPanel(new BorderLayout());
//			contentPane.add(therealinspector, BorderLayout.CENTER);
//			contentPane.setName("Syntax Inspector");
//			editor = null;
//			this.wfe.addToolWindow(contentPane);
//			Observer<Object> obs = new Observer<Object>() {
//				@Override
//				public void notify(Object event) {
//					update();
//				}
//			};
////			this.m.getSelectionChangeObservable().addObserver(obs);
//			this.view.getObservable().addObserver(obs);
//			this.m.getWorkflowCheckObservable().addObserver(obs);
////			this.m.getWorkflowObservable().addObserver(obs);
////			this.m.getChildObservable().addObserver(obs);
//			this.wfe.focusToolWindow(contentPane);
//			update();
//		}
//
//		public void update() {
////			WorkflowSelection ws = m.getSelection();
//			List<AbstractView> ws = view.getCurrentSelection();
////			if (ws == null)
////				ws = new WorkflowSelection(m.getRoot());
//			// set inspector text
//			{
//				InspectorialVisitor visitor = new InspectorialVisitor();
//				if (ws != null) // <--- always true for now
//				//	ws.visit(visitor);
//				{
//					for (AbstractView av : ws) {
//						av.visit(visitor, view);
//						
//					}
//				}
//				inspector.setText(visitor.getInspection());
//			}
//			// create editor
//			if (ws != this.ws || editor == null) {
//				EditorialVisitor visitor = new EditorialVisitor(eefs,
//						wfe.getApplication());
//				if (ws != null) // <--- always true for now
////					ws.visit(visitor);
//				{
//					for (AbstractView av : ws) {
//						av.visit(visitor, view);
//					}
//					
//					
//				}
//				if (visitor.getEditor() != editor) {
//					if (editor != null) {
//						contentPane.remove(editor);
//						editor = null;
//					}
//					editor = visitor.getEditor();
//					if (editor != null)
//						contentPane.add(editor, BorderLayout.EAST);
//					contentPane.validate();
//				}
//			}
//			this.ws = ws;
//		}
//	}
//
//	public InspectorTool(ElementEditorFactories eefs) {
//		this.eefs = eefs;
//	}
//
//	@Override
//	public Object instantiate(WorkflowEditor wfe) {
//		return new Inspector(wfe);
//	}
//
//
//}
