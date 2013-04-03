package org.vanda.studio.modules.workflows.run;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

import org.vanda.fragment.model.Model;
import org.vanda.studio.modules.workflows.inspector.AbstractEditorFactory;
import org.vanda.studio.modules.workflows.inspector.AbstractPreviewFactory;
import org.vanda.studio.modules.workflows.inspector.EditorialVisitor;
import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.inspector.InspectorialVisitor;
import org.vanda.studio.modules.workflows.inspector.PreviewesqueVisitor;
import org.vanda.studio.modules.workflows.model.WorkflowDecoration.WorkflowSelection;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;
import org.vanda.util.Action;
import org.vanda.util.Observer;

public class InspectorTool implements SemanticsToolFactory {

	private final ElementEditorFactories eefs;

	public final class Inspector {
		private final WorkflowEditor wfe;
		private final Model mm;
		private final JPanel contentPane;
		private final JEditorPane inspector;
		private final JScrollPane therealinspector;
		private JComponent editor;
		private JComponent preview;
		private WorkflowSelection ws;

		public Inspector(WorkflowEditor wfe, Model mm) {
			this.wfe = wfe;
			this.mm = mm;
			ws = null;
			inspector = new JEditorPane("text/html", "");
			inspector.setEditable(false);
			Font font = UIManager.getFont("Label.font");
	        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
	                "font-size: " + font.getSize() + "pt; }";
	        ((HTMLDocument) inspector.getDocument()).getStyleSheet().addRule(bodyRule);
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
			wfe.addAction(new CheckWorkflowAction(),
					KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
			this.wfe.getWorkflowDecoration().getSelectionChangeObservable().addObserver(obs);
			this.wfe.focusToolWindow(contentPane);
			update();
		}
		
		public void setEditor(AbstractEditorFactory editorFactory) {
			if (editor != null) {
				contentPane.remove(editor);
				editor = null;
			}
			if (editorFactory != null) {
				editor = editorFactory.createEditor(wfe.getApplication());
				if (editor != null)
					contentPane.add(editor, BorderLayout.EAST);
			}
			contentPane.validate();
		}
		
		public void setInspection(String inspection) {
			inspector.setText(inspection);
		}
		
		public void setPreview(AbstractPreviewFactory previewFactory) {
			if (preview != null) {
				contentPane.remove(preview);
				contentPane.remove(therealinspector);
				preview = null;
			}
			if (previewFactory != null) {
				preview = previewFactory.createPreview(wfe.getApplication());
				contentPane.add(therealinspector, BorderLayout.NORTH);
				contentPane.add(preview, BorderLayout.CENTER);
			} else
				contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.validate();
		}

		public void update() {
			ws = wfe.getWorkflowDecoration().getSelection();
			if (ws == null)
				ws = new WorkflowSelection(wfe.getWorkflowDecoration().getRoot());
			setInspection(InspectorialVisitor.inspect(mm, ws));
			setEditor(EditorialVisitor.createAbstractFactory(eefs, ws));
			setPreview(PreviewesqueVisitor.createPreviewFactory(mm, ws));
		}

		protected class CheckWorkflowAction implements Action {

			@Override
			public String getName() {
				return "Check Workflow";
			}

			@Override
			public void invoke() {
				mm.checkWorkflow();
			}
		}
	}

	public InspectorTool(ElementEditorFactories eefs) {
		this.eefs = eefs;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, Model model) {
		return new Inspector(wfe, model);
	}

}
