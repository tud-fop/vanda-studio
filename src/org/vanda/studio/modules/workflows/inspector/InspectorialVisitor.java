package org.vanda.studio.modules.workflows.inspector;

import java.util.List;
import java.util.Locale;

import org.vanda.view.View;
import org.vanda.view.Views.*;
import org.vanda.types.Type;
import org.vanda.workflows.data.SemanticAnalysis;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.hyper.ConnectionKey;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;
import org.vanda.workflows.hyper.SyntaxAnalysis;

public final class InspectorialVisitor implements SelectionVisitor {

	private final StringBuilder sb;
	private final SyntaxAnalysis synA; 
	private final SemanticAnalysis semA;
	
	public InspectorialVisitor(final SyntaxAnalysis synA, final SemanticAnalysis semA) {
		sb = new StringBuilder();
		this.synA = synA;
		this.semA = semA;
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
		Location variable = MutableWorkflow.getConnectionValue(cc);
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
		Type type = synA.getType(variable);
		if (type != null) {
			sb.append("<tr><th align=left>Value</th><td>");
			sb.append(semA.getDFA().getValue(variable));
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
		sb.append("</table></td></tr>");
		sb.append("</table>");
		sb.append("<table><tr><th align=left>Description</th></tr>");
		sb.append("<tr><td>");
		sb.append(j.getElement().getDescription());
		sb.append("</td></tr>");
		sb.append("</table></html>");
	}

	public String getInspection() {
		return sb.toString();
	}

	@Override
	public void visitVariable(MutableWorkflow iwf, Location variable) {
		sb.append("<html><table><tr><th colspan=5 align=left>Location ");
		sb.append(Integer.toHexString(variable.hashCode()));
		sb.append("</th></tr>");
		// sb.append("<dt>Variable</dt><dd>x");
		// sb.append(variable.toString());
		Type type = synA.getType(variable);
		if (type != null) {
			sb.append("<tr><th align=left>Value</th><td>");
			sb.append(semA.getDFA().getValue(variable));
			sb.append("</td><td>&nbsp;</td><th align=left>Type</th><td>");
			sb.append(type.toString());
			sb.append("</td></tr>");
		}
		sb.append("</table></html>");
	}
	
	public static String inspect(SyntaxAnalysis synA, SemanticAnalysis semA, View view) {
		InspectorialVisitor visitor = new InspectorialVisitor(synA, semA);
		// size == 1, to avoid arbitrary inspection in case of multi selection
		List<SelectionObject> sos = view.getCurrentSelection();
		if (sos.size() == 1) {
			for (SelectionObject so : sos)
				so.visit(visitor, view.getWorkflow());
			return visitor.getInspection();
		} else {
			visitor.visitWorkflow(view.getWorkflow());
			return visitor.getInspection();
		}
	}

}