package org.vanda.studio.modules.workflows.inspector;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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

	private final ElementEditorFactories eefs;

	private final class TheSelectionVisitor implements SelectionVisitor,
			RepositoryItemVisitor {

		public Model m;
		public JComponent newEditor = null;
		public String newInspection = "";

		public TheSelectionVisitor(Model m, WorkflowSelection oldSelection,
				WorkflowSelection newSelection, JComponent oldEditor) {
			this.m = m;
			if (oldSelection == newSelection)
				newEditor = oldEditor;
		}

		@Override
		public void visitWorkflow(List<Token> path, MutableWorkflow wf) {
			newInspection = wf.getName();
			if (newEditor == null)
				newEditor = eefs.workflowFactories.createEditor(wf);
		}

		@Override
		public void visitConnection(List<Token> path, Token address,
				MutableWorkflow wf, Connection cc) {
			StringBuilder sb = new StringBuilder();
			Token variable = wf.getVariable(address);
			ImmutableWorkflow iwf = null;
			Type type = null;
			if (m.getFrozen() != null)
				iwf = m.getFrozen().dereference(path.listIterator());
			if (iwf != null)
				type = iwf.getType(variable);
			sb.append("<html><h1>Connection</h1><dl>");
			sb.append("<dt>Source</dt><dd>");
			sb.append(wf.getChild(cc.source).getItem().getName());
			sb.append("</dd><dt>Source Port</dt><dd>");
			sb.append(cc.sourcePort);
			sb.append("</dd><dt>Target</dt><dd>");
			sb.append(wf.getChild(cc.target).getItem().getName());
			sb.append("</dd><dt>Target Port</dt><dd>");
			sb.append(cc.targetPort);
			sb.append("</dd><dt>Variable</dt><dd>x");
			sb.append(variable.toString());
			if (type != null) {
				sb.append("</dd><dt>Type</dt><dd>");
				sb.append(type.toString());
			}
			sb.append("</dd></dl></html>");
			newInspection = sb.toString();
			if (newEditor == null)
				newEditor = eefs.connectionFactories.createEditor(cc);

		}

		@Override
		public void visitJob(List<Token> path, Token address,
				MutableWorkflow wf, Job j) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><h1>");
			sb.append(j.getItem().getName());
			sb.append("</h1><dl><dt>Contact</dt><dd>");
			sb.append(j.getItem().getContact());
			sb.append("</dd><dt>Category</dt><dd>");
			sb.append(j.getItem().getCategory());
			sb.append("</dd></dl>");
			sb.append("<h2>Ports</h2><table width=\"400px\"><tr><th>Input Ports</th>");
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
			sb.append("<p>");
			sb.append(j.getItem().getDescription());
			sb.append("</p>");
			sb.append("</html>");
			newInspection = sb.toString();
			if (newEditor == null)
				j.getItem().visit(this);
		}

		@Override
		public void visitChoice(Choice c) {
			newEditor = eefs.choiceFactories.createEditor(c);
		}

		@Override
		public void visitInputPort(InputPort i) {
			newEditor = eefs.inputPortFactories.createEditor(i);
		}

		@Override
		public void visitLinker(Linker l) {
			newEditor = eefs.linkerFactories.createEditor(l);
		}

		@Override
		public void visitLiteral(Literal l) {
			newEditor = eefs.literalFactories.createEditor(l);
		}

		@Override
		public void visitOutputPort(OutputPort o) {
			newEditor = eefs.outputPortFactories.createEditor(o);
		}

		@Override
		public void visitTool(Tool t) {
			newEditor = eefs.toolFactories.createEditor(t);
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
			this.m.getNameChangeObservable().addObserver(obs);
			this.m.getModifyObservable().addObserver(obs);
			this.wfe.focusToolWindow(contentPane);
		}

		public void update() {
			WorkflowSelection ws = m.getSelection();
			TheSelectionVisitor visitor = new TheSelectionVisitor(m, this.ws,
					ws, editor);
			if (ws != null)
				ws.visit(m.getRoot(), visitor);
			inspector.setText(visitor.newInspection);
			if (visitor.newEditor != editor) {
				if (editor != null) {
					contentPane.remove(editor);
					editor = null;
				}
				editor = visitor.newEditor;
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
