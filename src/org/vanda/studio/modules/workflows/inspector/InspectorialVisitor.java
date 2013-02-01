package org.vanda.studio.modules.workflows.inspector;

import java.util.Locale;

import org.vanda.studio.modules.workflows.model.Model.SelectionVisitor;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

public final class InspectorialVisitor implements SelectionVisitor {

	private final StringBuilder sb;

	public InspectorialVisitor() {
		sb = new StringBuilder();
	}

	@Override
	public void visitWorkflow(MutableWorkflow iwf) {
		sb.append("<html><h1>");
		sb.append(iwf.getName());
		sb.append("</h1>");
		// Type type = iwf.getFragmentType();
		// sb.append("<dl>");
		// if (type != null) {
		// sb.append("</dd><dt>Type</dt><dd>");
		// sb.append(type.toString());
		// }
		// sb.append("</dl>");
	}

	@Override
	public void visitConnection(MutableWorkflow iwf, ConnectionKey cc) {
		Location variable = iwf.getConnectionValue(cc);
		// Type type = iwf.getType(variable);
		// Job sjob = wf.getChild(cc.source);
		sb.append("<html><h1>Connection</h1><dl>");
		// sb.append("<dt>Source</dt><dd>");
		// sb.append(sjob.getElement().getName());
		// sb.append("</dd><dt>Source Port</dt><dd>");
		// sb.append(sjob.getOutputPorts().get(cc.sourcePort).getIdentifier());
		sb.append("</dd><dt>Target</dt><dd>");
		sb.append(cc.target.getElement().getName());
		sb.append("</dd><dt>Target Port</dt><dd>");
		sb.append(cc.targetPort.getIdentifier());
		sb.append("</dd><dt>Variable</dt><dd>x");
		sb.append(variable.toString());
		// if (type != null) {
		// sb.append("</dd><dt>Type</dt><dd>");
		// sb.append(type.toString());
		// }
		sb.append("</dd></dl></html>");

	}

	@Override
	public void visitJob(MutableWorkflow wf, Job j) {
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
		// j.visit(this);
		sb.append("</html>");
	}

	public String getInspection() {
		return sb.toString();
	}

	@Override
	public void visitVariable(Location variable, MutableWorkflow iwf) {
		// Type type = iwf.getType(variable);
		sb.append("<html><h1>Location</h1><dl>");
		sb.append("</dd><dt>Variable</dt><dd>x");
		sb.append(variable.toString());
		// if (type != null) {
		// sb.append("</dd><dt>Type</dt><dd>");
		// sb.append(type.toString());
		// }
		sb.append("</dd></dl></html>");
	}

}