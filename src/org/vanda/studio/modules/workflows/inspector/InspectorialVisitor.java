package org.vanda.studio.modules.workflows.inspector;

import java.util.Locale;


import org.vanda.view.AbstractView;
import org.vanda.view.AbstractView.SelectionVisitor;
import org.vanda.view.View;
import org.vanda.fragment.model.Model;
import org.vanda.types.Type;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

public final class InspectorialVisitor implements SelectionVisitor {

	private final Model mm;
	private final StringBuilder sb;
	public InspectorialVisitor(final Model mm) {
		sb = new StringBuilder();
		this.mm = mm;
	}

	@Override
	public void visitWorkflow(MutableWorkflow iwf) {
		sb.append("<html><table><tr><th>");
		sb.append(iwf.getName());
		sb.append("</th></tr></table></html>");
		/*
		sb.append("<html><h1>");
		sb.append(iwf.getName());
		sb.append("</h1>");
		Type type = mm.getFragmentType();
		sb.append("<dl>");
		if (type != null) {
			sb.append("</dd><dt>Type</dt><dd>");
			sb.append(type.toString());
		}
		sb.append("</dl>");
		*/
	}

	@Override
	public void visitConnection(MutableWorkflow iwf, ConnectionKey cc) {
		Location variable = iwf.getConnectionValue(cc);
		ConnectionKey source = iwf.getVariableSource(variable);
		sb.append("<html><table><tr><th colspan=5 align=left>Connection with Location ");
		sb.append(Integer.toHexString(variable.hashCode()));
		sb.append("</th></tr>");
		sb.append("<tr><th align=left>Source</th><td>");
		sb.append(source.target.getElement().getName());
		sb.append('.');
		sb.append(source.targetPort.getIdentifier());
		sb.append("</td><td>&nbsp;</td><th align=left>Target</th><td>");
		sb.append(cc.target.getElement().getName());
		sb.append('.');
		sb.append(cc.targetPort.getIdentifier());
		sb.append("</td></tr>");
		Type type = mm.getType(variable);
		if (type != null) {
			sb.append("<tr><th align=left>Value</th><td>");
			sb.append(mm.getDataflowAnalysis().getValue(variable));
			sb.append("</td><td>&nbsp;</td><th align=left>Type</th><td>");
			sb.append(type.toString());
			sb.append("</td></tr>");
		}
		sb.append("</table></html>");

	}

	@Override
	public void visitJob(MutableWorkflow wf, Job j) {
		sb.append("<html><table><tr><th colspan=5 align=left>");
		sb.append(j.getElement().getName());
		sb.append("</th></tr>");
		sb.append("<tr><th align=left>Category</th><td>");
		sb.append(j.getElement().getCategory());
		sb.append("</td><td>&nbsp;</td><th align=left>Contact</th><td>");
		sb.append(j.getElement().getContact());
		sb.append("</td></tr>");
		// sb.append("<tr><th align=center colspan=2>Inputs</th><td></td><th align=center colspan=2>Outputs</th></tr>");
		sb.append("<tr><th valign=top align=left>Inputs</th><td valign=top><table><tr><th align=left>Identifier</th><th align=left>Type</th></tr>");
		for (Port p : j.getInputPorts()) {
			sb.append("<tr><td>");
			sb.append(p.getIdentifier().toLowerCase(Locale.ENGLISH));
			sb.append("</td><td>");
			sb.append(p.getType());
			sb.append("</td></tr>");
		}
		sb.append("</table></td><td>&nbsp;</td><th valign=top align=left>Outputs</th><td valign=top><table><tr><th align=left>Identifier</th><th align=left>Type</th></tr>");
		for (Port p : j.getOutputPorts()) {
			sb.append("<tr><td>");
			sb.append(p.getIdentifier().toLowerCase(Locale.ENGLISH));
			sb.append("</td><td>");
			sb.append(p.getType());
			sb.append("</td></tr>");
		}
		/*
		ListIterator<Port> ips = j.getInputPorts().listIterator();
		ListIterator<Port> ops = j.getOutputPorts().listIterator();
		while (ips.hasNext() || ops.hasNext()) {
			Port ip = ips.hasNext() ? ips.next() : null;
			Port op = ops.hasNext() ? ops.next() : null;
			sb.append("<tr>");
			sb.append("<td>");
			if (ip != null)
				sb.append(ip.getIdentifier().toLowerCase(Locale.ENGLISH));
			sb.append("</td>");
			sb.append("<td>");
			if (ip != null)
				sb.append(ip.getType());
			sb.append("</td>");
			// sb.append("</tr><tr>");
			sb.append("<td>");
			if (op != null)
				sb.append(op.getIdentifier().toLowerCase(Locale.ENGLISH));
			sb.append("</td>");
			sb.append("<td>");
			if (op != null)
				sb.append(op.getType());
			sb.append("</td>");
			sb.append("</tr>");
		}*/
		sb.append("</table></td></tr>");
		sb.append("</table></html>");
		/*
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
		*/
	}

	public String getInspection() {
		return sb.toString();
	}

	@Override
	public void visitVariable(Location variable, MutableWorkflow iwf) {
		sb.append("<html><table><tr><th colspan=5 align=left>Location ");
		sb.append(Integer.toHexString(variable.hashCode()));
		sb.append("</th></tr>");
		// sb.append("<dt>Variable</dt><dd>x");
		// sb.append(variable.toString());
		Type type = mm.getType(variable);
		if (type != null) {
			sb.append("<tr><th align=left>Value</th><td>");
			sb.append(mm.getDataflowAnalysis().getValue(variable));
			sb.append("</td><td>&nbsp;</td><th align=left>Type</th><td>");
			sb.append(type.toString());
			sb.append("</td></tr>");
		}
		sb.append("</table></html>");
	}
	
	public static String inspect(Model mm, View view) {
		InspectorialVisitor visitor = new InspectorialVisitor(mm);
		// size == 1, to avoid arbitrary inspection in case of multi selection
		if (view.getCurrentSelection().size() == 1) {
			for (AbstractView av : view.getCurrentSelection())
				av.visit(visitor, view);
			return visitor.getInspection();
		} else {
			view.getWorkflowView().visit(visitor, view);
			return visitor.getInspection();
		}
	}

}