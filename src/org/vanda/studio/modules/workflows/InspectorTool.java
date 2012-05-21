package org.vanda.studio.modules.workflows;

import java.awt.BorderLayout;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.vanda.studio.model.elements.Port;
import org.vanda.studio.model.hyper.AtomicJob;
import org.vanda.studio.model.hyper.CompositeJob;
import org.vanda.studio.model.hyper.Connection;
import org.vanda.studio.model.hyper.HyperWorkflow;
import org.vanda.studio.model.hyper.Job;
import org.vanda.studio.model.immutable.ImmutableWorkflow;
import org.vanda.studio.model.types.Type;
import org.vanda.studio.modules.workflows.Model.ConnectionSelection;
import org.vanda.studio.modules.workflows.Model.JobSelection;
import org.vanda.studio.modules.workflows.Model.WorkflowSelection;
import org.vanda.studio.util.Observer;
import org.vanda.studio.util.TokenSource.Token;

public class InspectorTool implements ToolFactory {

	private final List<ElementEditorFactory> eefs;

	public static final class Inspector {
		private final WorkflowEditor wfe;
		private final Model<?> m;
		private final JPanel contentPane;
		private final JEditorPane inspector;
		private final JScrollPane therealinspector;
		private JComponent editor;
		private final List<ElementEditorFactory> eefs;

		public Inspector(WorkflowEditor wfe, Model<?> m,
				List<ElementEditorFactory> eefs) {
			this.wfe = wfe;
			this.m = m;
			this.eefs = eefs;
			inspector = new JEditorPane("text/html", "");
			inspector.setEditable(false);
			therealinspector = new JScrollPane(inspector);
			contentPane = new JPanel(new BorderLayout());
			contentPane.add(therealinspector, BorderLayout.CENTER);
			contentPane.setName("Inspector");
			editor = null;
			this.wfe.addToolWindow(contentPane);
			this.m.getSelectionChangeObservable().addObserver(
					new Observer<Model<?>>() {
						@Override
						public void notify(Model<?> event) {
							update();
						}
					});
			this.m.getWorkflowCheckObservable().addObserver(
					new Observer<Model<?>>() {
						@Override
						public void notify(Model<?> event) {
							update();
						}
					});
		}

		JComponent createEditor(Object o) {
			JComponent result = null;
			ListIterator<ElementEditorFactory> li = eefs.listIterator();
			while (result == null && li.hasNext())
				result = li.next().createEditor(o);
			return result;
		}

		public void update() {
			WorkflowSelection ws = m.getSelection();
			if (editor != null) {
				contentPane.remove(editor);
				editor = null;
			}
			if (ws == null) {
				inspector.setText("");
			} else if (ws instanceof JobSelection) {
				Job<?> j = m.getRoot().dereference(ws.path.listIterator())
						.getChild(((JobSelection) ws).address);
				StringBuilder sb = new StringBuilder();
				sb.append("<html><h1>");
				sb.append(j.getName());
				sb.append("</h1><dl><dt>Contact</dt><dd>");
				sb.append(j.getContact());
				sb.append("</dd><dt>Category</dt><dd>");
				sb.append(j.getCategory());
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
				sb.append(j.getDescription());
				sb.append("</p>");
				sb.append("</html>");
				inspector.setText(sb.toString());
				if (j instanceof AtomicJob<?>)
					editor = createEditor(((AtomicJob<?>) j).getElement());
				else if (j instanceof CompositeJob<?, ?>)
					editor = createEditor(((CompositeJob<?, ?>) j).getLinker());
			} else if (ws instanceof ConnectionSelection) {
				StringBuilder sb = new StringBuilder();
				HyperWorkflow<?> wf = m.getRoot().dereference(
						ws.path.listIterator());
				Connection cc = wf
						.getConnection(((ConnectionSelection) ws).address);
				Token variable = wf
						.getVariable(((ConnectionSelection) ws).address);
				ImmutableWorkflow<?> iwf = null;
				Type type = null;
				if (m.getFrozen() != null)
					iwf = m.getFrozen().dereference(ws.path.listIterator());
				if (iwf != null)
					type = iwf.getType(variable);
				sb.append("<html><h1>Connection</h1><dl>");
				sb.append("<dt>Source</dt><dd>");
				sb.append(wf.getChild(cc.source).getName());
				sb.append("</dd><dt>Source Port</dt><dd>");
				sb.append(cc.sourcePort);
				sb.append("</dd><dt>Target</dt><dd>");
				sb.append(wf.getChild(cc.target).getName());
				sb.append("</dd><dt>Target Port</dt><dd>");
				sb.append(cc.targetPort);
				sb.append("</dd><dt>Variable</dt><dd>x");
				sb.append(variable.toString());
				if (type != null) {
					sb.append("</dd><dt>Type</dt><dd>");
					sb.append(type.toString());
				}
				sb.append("</dd></dl></html>");
				inspector.setText(sb.toString());
				editor = createEditor(cc);
			} else {
				HyperWorkflow<?> hwf = m.getRoot().dereference(
						ws.path.listIterator());
				inspector.setText(hwf.getName());
				editor = createEditor(hwf);
			}
			if (editor != null)
				contentPane.add(editor, BorderLayout.EAST);
			contentPane.validate();
		}
	}

	public InspectorTool(List<ElementEditorFactory> eefs) {
		this.eefs = eefs;
	}

	@Override
	public Object instantiate(WorkflowEditor wfe, Model<?> m) {
		return new Inspector(wfe, m, eefs);
	}

}
