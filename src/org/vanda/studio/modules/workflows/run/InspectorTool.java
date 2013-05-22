package org.vanda.studio.modules.workflows.run;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import org.vanda.studio.app.WindowSystem;
import org.vanda.studio.modules.workflows.inspector.AbstractEditorFactory;
import org.vanda.studio.modules.workflows.inspector.AbstractPreviewFactory;
import org.vanda.studio.modules.workflows.inspector.EditorialVisitor;
import org.vanda.studio.modules.workflows.inspector.ElementEditorFactories;
import org.vanda.studio.modules.workflows.inspector.InspectorialVisitor;
import org.vanda.studio.modules.workflows.inspector.PreviewesqueVisitor;
import org.vanda.studio.modules.workflows.model.WorkflowEditor;

import org.vanda.util.Observer;
import org.vanda.view.View;
import org.vanda.view.View.GlobalViewEvent;
import org.vanda.view.View.GlobalViewListener;
import org.vanda.util.Action;

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
		private final View view;

		public Inspector(WorkflowEditor wfe, Model mm, View view) {
			this.wfe = wfe;
			this.mm = mm;
			this.view = view;

			// ws = null;
			inspector = new JEditorPane("text/html", "");
			inspector.setEditable(false);
			Font font = UIManager.getFont("Label.font");
			String bodyRule = "body { font-family: " + font.getFamily() + "; "
					+ "font-size: " + font.getSize() + "pt; }";
			((HTMLDocument) inspector.getDocument()).getStyleSheet().addRule(
					bodyRule);
			therealinspector = new JScrollPane(inspector);
			contentPane = new JPanel(new BorderLayout());
			contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.setPreferredSize(new Dimension(800, 300));
			contentPane.setName("Inspector");
			editor = null;
			this.wfe.addToolWindow(contentPane, WindowSystem.SOUTH);

			final GlobalViewListener<View> listener = new GlobalViewListener<View>() {

				@Override
				public void markChanged(View v) {
					// do nothing
				}

				@Override
				public void selectionChanged(View v) {
					update();
				}

			};
			wfe.addAction(new CheckWorkflowAction(),
					KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));

			wfe.getView().getObservable()
					.addObserver(new Observer<GlobalViewEvent<View>>() {

						@Override
						public void notify(GlobalViewEvent<View> event) {
							event.doNotify(listener);
						}
					});

			wfe.getView().getWorkflow().getObservable()
					.addObserver(new Observer<Object>() {

						@Override
						public void notify(Object event) {
							update();
						}

					});

			this.wfe.focusToolWindow(contentPane);
			update();
		}

		public void setEditor(AbstractEditorFactory editorFactory) {
			if (editor != null) {
				contentPane.remove(editor);
				editor = null;
			}
			if (editorFactory != null) {
				editor = editorFactory.createEditor(wfe.getDatabase());
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
				preview = null;
			}
			contentPane.remove(therealinspector);
			if (previewFactory != null) {
				preview = previewFactory.createPreview(wfe.getApplication());
				contentPane.add(therealinspector, BorderLayout.NORTH);
				contentPane.add(preview, BorderLayout.CENTER);
			} else
				contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.validate();
		}

		public void update() {

			// WorkflowSelection newws =
			// wfe.getWorkflowDecoration().getSelection();
			// List<AbstractView> ws = wfe.getView().getCurrentSelection();

			// WorkflowSelection truews = newws;
			// if (truews == null)
			// truews = new
			// WorkflowSelection(wfe.getWorkflowDecoration().getRoot());
			setInspection(InspectorialVisitor.inspect(mm, view));
			// if (newws != ws) {
			// editor and preview keep track of changes on their own
			setEditor(EditorialVisitor.createAbstractFactory(eefs, view));
			setPreview(PreviewesqueVisitor.createPreviewFactory(mm, view));
			// }
			// ws = newws;
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
	public Object instantiate(WorkflowEditor wfe, Model model, View view) {
		return new Inspector(wfe, model, view);
	}
}
