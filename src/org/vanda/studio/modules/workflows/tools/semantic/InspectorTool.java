package org.vanda.studio.modules.workflows.tools.semantic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

import org.vanda.studio.app.SplitTabbedPane;

//import org.vanda.fragment.model.Model;

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
import org.vanda.view.Views.*;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public class InspectorTool implements SemanticsToolFactory {

	private final ElementEditorFactories eefs;

	public final class Inspector {
		private final WorkflowEditor wfe;
		private final Observer<Object> workflowObserver;
		// private final Model mm;
		private final SyntaxAnalysis synA;
		private final SemanticAnalysis semA;
		private final Observer<SemanticAnalysis> semanticObserver;
		private final JPanel contentPane;
		private final JPanel panNorth;
		private final JEditorPane inspector;
		private final JScrollPane therealinspector;
		private JComponent editor;
		private JComponent preview;
		private final View view;
		private final Observer<ViewEvent<View>> viewObserver;

		// public Inspector(WorkflowEditor wfe, Model mm, View view) {
		public Inspector(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA, View view) {
			this.wfe = wfe;
			// this.mm = mm;
			this.synA = synA;
			this.semA = semA;
			this.view = view;

			// ws = null;
			inspector = new JEditorPane("text/html", "");
			inspector.setEditable(false);
			Font font = UIManager.getFont("Label.font");
			String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize()
					+ "pt; }";
			((HTMLDocument) inspector.getDocument()).getStyleSheet().addRule(bodyRule);

			therealinspector = new JScrollPane(inspector);
			contentPane = new JPanel(new BorderLayout());
			contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.setPreferredSize(new Dimension(800, 300));
			contentPane.setName("Inspector");
			panNorth = new JPanel(new BorderLayout());
			editor = null;
			
			this.wfe.addSideSplit(contentPane, SplitTabbedPane.Side.SOUTH, 400);

			final ViewListener<View> listener = new ViewListener<View>() {
				@Override
				public void markChanged(View v) {
					// do nothing
				}

				@Override
				public void selectionChanged(View v) {
					update();
				}

				@Override
				public void highlightingChanged(View v) {
					// do nothing
				}
			};

			viewObserver = new Observer<ViewEvent<View>>() {

				@Override
				public void notify(ViewEvent<View> event) {
					event.doNotify(listener);
				}
			};
			wfe.getView().getObservable().addObserver(viewObserver);

			workflowObserver = new Observer<Object>() {

				@Override
				public void notify(Object event) {
					update();
				}

			};
			wfe.getView().getWorkflow().getObservable().addObserver(workflowObserver);

			semanticObserver = new Observer<SemanticAnalysis>() {

				@Override
				public void notify(SemanticAnalysis event) {
					update();
				}

			};
			semA.getObservable().addObserver(semanticObserver);

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
			contentPane.remove(panNorth);
			if (preview != null) {
				contentPane.remove(preview);
				preview = null;
			}
			if (previewFactory != null) {
				preview = previewFactory.createPreview(wfe.getApplication());
				JComponent buttons = previewFactory.createButtons(wfe.getApplication());
				panNorth.removeAll();
				panNorth.add(therealinspector, BorderLayout.CENTER);
				panNorth.add(buttons, BorderLayout.EAST);
				contentPane.add(panNorth, BorderLayout.NORTH);
				contentPane.add(preview, BorderLayout.CENTER);
			} else
				contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.validate();
		}

		public void update() {
			setInspection(InspectorialVisitor.inspect(synA, semA, view));
			// editor and preview keep track of changes on their own
			setEditor(EditorialVisitor.createAbstractFactory(eefs, view));
			setPreview(PreviewesqueVisitor.createPreviewFactory(semA, synA, view));
		}

	}

	public InspectorTool(ElementEditorFactories eefs) {
		this.eefs = eefs;
	}

	@Override
	// public Object instantiate(WorkflowEditor wfe, Model model, View view) {
	public Object instantiate(WorkflowEditor wfe, SyntaxAnalysis synA, SemanticAnalysis semA, View view) {
		return new Inspector(wfe, synA, semA, view);
	}
}
