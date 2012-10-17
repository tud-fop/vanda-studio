package org.vanda.studio.modules.workflows.inspector;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vanda.studio.app.Application;
import org.vanda.studio.model.elements.Choice;
import org.vanda.studio.model.elements.InputPort;
import org.vanda.studio.model.elements.Linker;
import org.vanda.studio.model.elements.Literal;
import org.vanda.studio.model.elements.OutputPort;
import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.elements.RepositoryItemVisitor;
import org.vanda.studio.model.elements.Tool;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.hyper.MutableWorkflow;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.workflows.Model;
import org.vanda.studio.modules.workflows.Model.SelectionVisitor;
import org.vanda.studio.modules.workflows.Model.WorkflowSelection;
import org.vanda.studio.modules.workflows.ToolFactory;
import org.vanda.studio.modules.workflows.WorkflowEditor;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource.Token;

public class InspectorTool implements ToolFactory {

	@SuppressWarnings("unchecked")
	private static final WorkflowSelection emptySelection = new WorkflowSelection(
			(List<Token>) Collections.EMPTY_LIST);

	private final ElementEditorFactories eefs;

	private final class TheSelectionVisitor extends RepositoryItemVisitor
			implements SelectionVisitor {

		private final Application app;
		private final Model model;
		private JComponent editor = null;
		private final StringBuilder sb;

		public TheSelectionVisitor(Application app, Model m,
				WorkflowSelection oldSelection, WorkflowSelection newSelection,
				JComponent oldEditor) {
			this.app = app;
			this.model = m;
			sb = new StringBuilder();
			if (oldSelection == newSelection)
				editor = oldEditor;
		}

		@Override
		public void visitWorkflow(List<Token> path, MutableWorkflow wf) {
			sb.append("<html><h1>");
			sb.append(wf.getName());
			sb.append("</h1>");
			ImmutableWorkflow iwf = null;
			Type type = null;
			if (model.getFrozen() != null) {
				iwf = model.getFrozen().dereference(path.listIterator());
				type = iwf.getFragmentType();
			}
			sb.append("<dl>");
			if (type != null) {
				sb.append("</dd><dt>Type</dt><dd>");
				sb.append(type.toString());
			}
			sb.append("</dl>");
			if (path.isEmpty() && model.getFrozen() != null) {
				sb.append("<h2>Pseudo code</h2><font size=-1><pre>");
				model.getFrozen().appendText(sb);
				sb.append("</pre></font>");
				if (!model.getFrozen().isSane()) {
					sb.append("Warning: Your workflow(s) are not executable!\n"
							+ "The most likely reason is that some input port "
							+ "is not connected.<p>");
				}
				sb.append("<h2>Instances</h2>\n");
				List<ImmutableWorkflow> iwfs = model.getUnfolded();
				for (ImmutableWorkflow i : iwfs) {
					sb.append("<hr><pre>");
					i.appendText(sb);
					sb.append("</pre><p>");
				}
			}
			if (editor == null)
				editor = eefs.workflowFactories.createEditor(app, wf);
		}

		@Override
		public void visitConnection(List<Token> path, Token address,
				MutableWorkflow wf, Connection cc) {
			Token variable = wf.getVariable(address);
			ImmutableWorkflow iwf = null;
			Type type = null;
			if (model.getFrozen() != null)
				iwf = model.getFrozen().dereference(path.listIterator());
			if (iwf != null)
				type = iwf.getType(variable);
			Job sjob = wf.getChild(cc.source);
			Job tjob = wf.getChild(cc.target);
			sb.append("<html><h1>Connection</h1><dl>");
			sb.append("<dt>Source</dt><dd>");
			sb.append(sjob.getItem().getName());
			sb.append("</dd><dt>Source Port</dt><dd>");
			sb.append(sjob.getOutputPorts().get(cc.sourcePort).getIdentifier());
			sb.append("</dd><dt>Target</dt><dd>");
			sb.append(tjob.getItem().getName());
			sb.append("</dd><dt>Target Port</dt><dd>");
			sb.append(tjob.getInputPorts().get(cc.targetPort).getIdentifier());
			sb.append("</dd><dt>Variable</dt><dd>x");
			sb.append(variable.toString());
			if (type != null) {
				sb.append("</dd><dt>Type</dt><dd>");
				sb.append(type.toString());
			}
			sb.append("</dd></dl></html>");
			if (editor == null)
				editor = eefs.connectionFactories.createEditor(app, cc);

		}

		@Override
		public void visitJob(List<Token> path, Token address,
				MutableWorkflow wf, Job j) {
			sb.append("<html><h1>");
			sb.append(j.getItem().getName());
			sb.append("</h1><dl><dt>Contact</dt><dd>");
			sb.append(j.getItem().getContact());
			sb.append("</dd><dt>Category</dt><dd>");
			sb.append(j.getItem().getCategory());
			sb.append("</dd></dl>");
			sb.append("<h2>Ports</h2><table width=\"400px\"><tr>"
					+ "<th>Input Ports</th>");
			sb.append("<th>Output Ports</th></tr>");
			sb.append("<tr><td><ul>");
			for (Port p : j.getInputPorts()) {
				sb.append("<li>");
				sb.append(p.getIdentifier().toLowerCase(Locale.ENGLISH));
				sb.append("<br>&nbsp; :: ");
				sb.append(p.getType());
				sb.append("</li>");
			}
			sb.append("</ul></td><td><ul>");
			for (Port p : j.getOutputPorts()) {
				sb.append("<li>");
				sb.append(p.getIdentifier().toLowerCase(Locale.ENGLISH));
				sb.append("<br>&nbsp; :: ");
				sb.append(p.getType());
				sb.append("</li>");
			}
			sb.append("</ul></td></tr></table>");
			sb.append("<h2>Description</h2>");
			// sb.append("<p>");
			sb.append(j.getItem().getDescription());
			sb.append("<p>");
			j.getItem().visit(this);
			sb.append("</html>");
		}

		@Override
		public void visitChoice(Choice c) {
			if (editor == null) {
				editor = eefs.choiceFactories.createEditor(app, c);
			}
		}

		@Override
		public void visitInputPort(InputPort i) {
			if (editor == null) {
				editor = eefs.inputPortFactories.createEditor(app, i);
			}
		}

		@Override
		public void visitLinker(Linker l) {
			if (editor == null) {
				sb.append("<h2>LinkerInfo</h2><dl>");
				sb.append("<dt>Fragment Type</dt><dd>");
				sb.append(":: " + l.getFragmentType());
				sb.append("</dd><dt>Inner Fragment Type</dt><dd>");
				sb.append(":: " + l.getInnerFragmentType());
				sb.append("</dd></dl>");
				editor = eefs.linkerFactories.createEditor(app, l);
			}
		}

		@Override
		public void visitLiteral(Literal l) {
			if (editor == null) {
				editor = eefs.literalFactories.createEditor(app, l);
			}
		}

		@Override
		public void visitOutputPort(OutputPort o) {
			if (editor == null) {
				editor = eefs.outputPortFactories.createEditor(app, o);
			}
		}

		@Override
		public void visitTool(Tool t) {
			if (editor == null) {
				editor = eefs.toolFactories.createEditor(app, t);
			}
		}

		public String getInspection() {
			return sb.toString();
		}

		public JComponent getEditor() {
			return editor;
		}

	}

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
			TheSelectionVisitor visitor = new TheSelectionVisitor(
					wfe.getApplication(), m, this.ws, ws, editor);
			if (ws != null) // <--- always true for now
				ws.visit(m.getRoot(), visitor);
			inspector.setText(visitor.getInspection());
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
