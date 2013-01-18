package org.vanda.studio.modules.workflows.tools;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vanda.studio.modules.workflows.inspector.EditorialVisitor;
import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.inspector.InspectorialVisitor;
import org.vanda.studio.modules.workflows.model.Model;
import org.vanda.studio.modules.workflows.model.ToolFactory;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.studio.modules.workflows.model.Model.WorkflowSelection;
import org.vanda.util.Observer;

public class InspectorTool implements ToolFactory {

	private final ElementEditorFactories eefs;

	public final class Inspector {
		private final WorkflowEditor wfe;
		private final Model m;
		private final JPanel contentPane;
		private final JEditorPane inspector;
		private final JScrollPane therealinspector;
		private JComponent editor;
		private WorkflowSelection ws;

		public Inspector(WorkflowEditor wfe) {
			this.wfe = wfe;
			this.m = wfe.getModel(); // XXX better not cache this
			ws = null;
			inspector = new JEditorPane("text/html", "");
			inspector.setEditable(false);
			therealinspector = new JScrollPane(inspector);
			contentPane = new JPanel(new BorderLayout());
			contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.setName("Syntax Inspector");
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
//			this.m.getWorkflowObservable().addObserver(obs);
//			this.m.getChildObservable().addObserver(obs);
			this.wfe.focusToolWindow(contentPane);
			update();
		}

		public void update() {
			WorkflowSelection ws = m.getSelection();
			if (ws == null)
				ws = new WorkflowSelection(m.getRoot());
			// set inspector text
			{
				InspectorialVisitor visitor = new InspectorialVisitor();
				if (ws != null) // <--- always true for now
					ws.visit(visitor);
				inspector.setText(visitor.getInspection());
			}
			// create editor
			if (ws != this.ws || editor == null) {
				EditorialVisitor visitor = new EditorialVisitor(eefs,
						wfe.getApplication());
				if (ws != null) // <--- always true for now
					ws.visit(visitor);
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
	public Object instantiate(WorkflowEditor wfe) {
		return new Inspector(wfe);
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
		return "";
	}

	@Override
	public String getId() {
		return "syntax-inspector";
	}

	@Override
	public String getName() {
		return "Syntax Inspector";
	}

	@Override
	public String getVersion() {
		return "2012-12-12";
	}

}
