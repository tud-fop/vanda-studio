package org.vanda.studio.modules.workflows.inspector;

import java.util.Locale;

import org.vanda.studio.modules.workflows.model.Model;
import org.vanda.studio.modules.workflows.model.Model.SelectionVisitor;
import org.vanda.types.Type;
import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.Connection;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.immutable.ImmutableWorkflow;

public final class InspectorialVisitor extends ElementVisitor implements
		SelectionVisitor {

	private final Model model;
	private final StringBuilder sb;

	public InspectorialVisitor(Model m) {
		this.model = m;
		sb = new StringBuilder();
	}

	@Override
	public void visitWorkflow(MutableWorkflow wf) {
		sb.append("<html><h1>");
		sb.append(wf.getName());
		sb.append("</h1>");
		ImmutableWorkflow iwf = null;
		Type type = null;
		if (model.getFrozen() != null) {
			iwf = model.getFrozen(); // XXX .dereference(path.listIterator());
			type = iwf.getFragmentType();
		}
		sb.append("<dl>");
		if (type != null) {
			sb.append("</dd><dt>Type</dt><dd>");
			sb.append(type.toString());
		}
		sb.append("</dl>");
		if (/* path.isEmpty() && */model.getFrozen() != null) {
			sb.append("<h2>Pseudo code</h2><font size=-1><pre>");
			model.getFrozen().appendText(sb);
			sb.append("</pre></font>");
			if (!model.getFrozen().isSane()) {
				sb.append("Warning: Your workflow(s) are not executable!\n"
						+ "The most likely reason is that some input port "
						+ "is not connected.<p>");
			}
			/*
			sb.append("<h2>Instances</h2>\n");
			List<ImmutableWorkflow> iwfs = model.getUnfolded();
			for (ImmutableWorkflow i : iwfs) {
				sb.append("<hr><pre>");
				i.appendText(sb);
				sb.append("</pre><p>");
			}
			*/
		}
	}

	@Override
	public void visitConnection(Token address, MutableWorkflow wf, Connection cc) {
		Token variable = wf.getVariable(address);
		ImmutableWorkflow iwf = null;
		Type type = null;
		if (model.getFrozen() != null)
			iwf = model.getFrozen(); // XXX .dereference(path.listIterator());
		if (iwf != null)
			type = iwf.getType(variable);
		Job sjob = wf.getChild(cc.source);
		Job tjob = wf.getChild(cc.target);
		sb.append("<html><h1>Connection</h1><dl>");
		sb.append("<dt>Source</dt><dd>");
		sb.append(sjob.getElement().getName());
		sb.append("</dd><dt>Source Port</dt><dd>");
		sb.append(sjob.getOutputPorts().get(cc.sourcePort).getIdentifier());
		sb.append("</dd><dt>Target</dt><dd>");
		sb.append(tjob.getElement().getName());
		sb.append("</dd><dt>Target Port</dt><dd>");
		sb.append(tjob.getInputPorts().get(cc.targetPort).getIdentifier());
		sb.append("</dd><dt>Variable</dt><dd>x");
		sb.append(variable.toString());
		if (type != null) {
			sb.append("</dd><dt>Type</dt><dd>");
			sb.append(type.toString());
		}
		sb.append("</dd></dl></html>");

	}

	@Override
	public void visitJob(Token address, MutableWorkflow wf, Job j) {
		sb.append("<html><h1>");
		sb.append(j.getElement().getName());
		sb.append("</h1><dl><dt>Contact</dt><dd>");
		sb.append(j.getElement().getContact());
		sb.append("</dd><dt>Category</dt><dd>");
		sb.append(j.getElement().getCategory());
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
		sb.append(j.getElement().getDescription());
		sb.append("<p>");
		j.getElement().visit(this);
		sb.append("</html>");
	}

	public String getInspection() {
		return sb.toString();
	}

	@Override
	public void visitVariable(Token variable, MutableWorkflow wf) {
		ImmutableWorkflow iwf = null;
		Type type = null;
		if (model.getFrozen() != null)
			iwf = model.getFrozen(); // XXX .dereference(path.listIterator());
		if (iwf != null)
			type = iwf.getType(variable);
		sb.append("<html><h1>Location</h1><dl>");
		sb.append("</dd><dt>Variable</dt><dd>x");
		sb.append(variable.toString());
		if (type != null) {
			sb.append("</dd><dt>Type</dt><dd>");
			sb.append(type.toString());
		}
		sb.append("</dd></dl></html>");
	}

}