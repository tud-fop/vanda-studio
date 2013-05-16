package org.vanda.studio.modules.workflows.run;


import java.util.List;
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

import org.vanda.types.Type;
import org.vanda.util.Observer;
import org.vanda.view.AbstractView;
import org.vanda.view.AbstractView.SelectionVisitor;
import org.vanda.view.View;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

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
//<<<<<<< HEAD
		private final View view;
//
//		private class InspectorialVisitor implements SelectionVisitor {
//
//			String value = null;
//			Type type = null;
//
//			@Override
//			public void visitWorkflow(MutableWorkflow wf) {
//			}
//
//			@Override
//			public void visitConnection(MutableWorkflow wf, ConnectionKey cc) {
//				visitVariable(wf.getConnectionValue(cc), wf);
//			}
//
//			@Override
//			public void visitJob(MutableWorkflow wf, Job j) {
//			}
//
//			@Override
//			public void visitVariable(Location variable, MutableWorkflow wf) {
//				type = wfe.getModel().getType(variable);
//				value = mm.getDataflowAnalysis().getValue(variable);
//				// XXX no support for nested workflows because wf is ignored
//			}
//
//		}
//=======
//		private WorkflowSelection ws;


		public Inspector(WorkflowEditor wfe, Model mm, View view) {
			this.wfe = wfe;
			this.mm = mm;
			this.view = view;
	
//			ws = null;
			inspector = new JEditorPane("text/html", "");
			inspector.setEditable(false);
			Font font = UIManager.getFont("Label.font");
	        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
	                "font-size: " + font.getSize() + "pt; }";
	        ((HTMLDocument) inspector.getDocument()).getStyleSheet().addRule(bodyRule);
			therealinspector = new JScrollPane(inspector);
			contentPane = new JPanel(new BorderLayout());
			contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.setPreferredSize(new Dimension(800, 300));
			contentPane.setName("Inspector");
			editor = null;
			this.wfe.addToolWindow(contentPane, WindowSystem.SOUTH);

			Observer<Object> obs = new Observer<Object>() {
				@Override
				public void notify(Object event) {
					update();
				}
			};
			wfe.addAction(new CheckWorkflowAction(),
					KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
//			this.wfe.getWorkflowDecoration().getSelectionChangeObservable().addObserver(obs);
			wfe.getView().getObservable().addObserver(obs);
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

//			WorkflowSelection newws = wfe.getWorkflowDecoration().getSelection();
			List<AbstractView> ws = wfe.getView().getCurrentSelection();

//			WorkflowSelection truews = newws;
//			if (truews == null)
//				truews = new WorkflowSelection(wfe.getWorkflowDecoration().getRoot());
			setInspection(InspectorialVisitor.inspect(mm, view));
//			if (newws != ws) {
				// editor and preview keep track of changes on their own
				setEditor(EditorialVisitor.createAbstractFactory(eefs, view));
				setPreview(PreviewesqueVisitor.createPreviewFactory(mm, view));
//>>>>>>> vanilla
//			}
//			ws = newws;
		}
	


	

//	@Override
//	public String getCategory() {
//		return "Workflow Inspection";
//	}


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
