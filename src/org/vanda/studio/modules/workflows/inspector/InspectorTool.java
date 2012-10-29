package org.vanda.studio.modules.workflows.inspector;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vanda.studio.app.ToolFactory;
import org.vanda.studio.app.WorkflowEditor;
import org.vanda.studio.model.Model;
import org.vanda.studio.model.Model.WorkflowSelection;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource.Token;

public class InspectorTool implements ToolFactory {

	@SuppressWarnings("unchecked")
	private static final WorkflowSelection emptySelection = new WorkflowSelection(
			(List<Token>) Collections.EMPTY_LIST);
	private final ElementEditorFactories eefs;

	public final class Inspector {
		private final WorkflowEditor wfe;
		private final Model m;
		private final JPanel contentPane;
		private final JEditorPane inspector;
		private final JScrollPane therealinspector;
		private JComponent editor;
		private WorkflowSelection ws;

		public Inspector(WorkflowEditor wfe, Model m) {
			this.wfe = wfe;
			this.m = m;
			ws = null;
			inspector = new JEditorPane("text/html", "");
			inspector.setEditable(false);
			therealinspector = new JScrollPane(inspector);
			contentPane = new JPanel(new BorderLayout());
			contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.setName("Inspector");
			editor = null;
			this.wfe.addToolWindow(contentPane);
			Observer<Object> obs = new Observer<Object>() {
				@Override
				public void notify(Object event) {
					update();
				}
			};
			this.m.getSelectionChangeObservable().addObserver(obs);
			this.m.getWorkflowCheckObservable().addObserver(obs);
			this.m.getWorkflowObservable().addObserver(obs);
			this.m.getChildObservable().addObserver(obs);
			this.wfe.focusToolWindow(contentPane);
			update();
		}

		public void update() {
			WorkflowSelection ws = m.getSelection();
			if (ws == null)
				ws = emptySelection;
			// set inspector text
			{
				InspectorialVisitor visitor = new InspectorialVisitor(m);
				if (ws != null) // <--- always true for now
					ws.visit(m.getRoot(), visitor);
				inspector.setText(visitor.getInspection());
			}
			// create editor
			if (ws != this.ws || editor == null) {
				EditorialVisitor visitor = new EditorialVisitor(eefs,
						wfe.getApplication());
				if (ws != null) // <--- always true for now
					ws.visit(m.getRoot(), visitor);
				if (visitor.getEditor() != editor) {
					if (editor != null) {
						contentPane.remove(editor);
						editor = null;
					}
					editor = visitor.getEditor();
					if (editor != null)
						contentPane.add(editor, BorderLayout.EAST);
					contentPane.validate();
				}
			}
			this.ws = ws;
		}
	}

	public InspectorTool(ElementEditorFactories eefs) {
		this.eefs = eefs;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, Model m) {
		return new Inspector(wfe, m);
	}

}
