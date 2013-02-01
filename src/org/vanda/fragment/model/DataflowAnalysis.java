package org.vanda.fragment.model;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.vanda.types.Type;
import org.vanda.workflows.elements.ElementVisitor;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Port;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.hyper.Job;
import org.vanda.workflows.hyper.Location;
import org.vanda.workflows.hyper.MutableWorkflow;

// XXX removed: handle ports (see older versions)
// XXX I've had it with all of this instanceof/typecast bullshit
// find a principled solution (e.g., visitor)
public final class DataflowAnalysis {
	public static final String UNDEFINED = "UNDEFINED";

	private final MutableWorkflow workflow;
	private final Map<Location, String> values;
	private final Job[] jobs;
	private final Type fragmentType;

	public DataflowAnalysis(MutableWorkflow iwf, Job[] sorted, Type fragmentType) {
		workflow = iwf;
		values = new HashMap<Location, String>();
		jobs = sorted;
		this.fragmentType = fragmentType;
	}

	public Type getFragmentType() {
		return fragmentType;
	}

	public Job[] getSorted() {
		return jobs;
	}

	public void init() {
		if (jobs == null)
			return;
		for (final Job ji : jobs) {
			if (ji.isConnected()) {
				ji.visit(new ElementVisitor() {
					@Override
					public void visitLiteral(Literal lit) {
						values.put(ji.bindings.get(ji.getOutputPorts().get(0)),
								lit.getValue());
					}

					@Override
					public void visitTool(Tool t) {
						StringBuilder sb = new StringBuilder();
						sb.append('(');
						List<Port> ports = t.getInputPorts();
						if (ports.size() > 0)
							appendValue(sb, ji, ports.get(0));
						for (int i = 1; i < ports.size(); i++) {
							sb.append(',');
							appendValue(sb, ji, ports.get(i));
						}
						sb.append(')');
						String s = md5sum(sb.toString());
						for (Port op : t.getOutputPorts()) {
							String value = Fragments.normalize(t.getId()) + "."
									+ s + "." + op.getIdentifier();
							values.put(ji.bindings.get(op), value);
						}

					}

				});
			} else {
				for (Port op : ji.getOutputPorts()) {
					Location variable = ji.bindings.get(op);
					StringBuilder sb = new StringBuilder();
					sb.append('[');
					sb.append(UNDEFINED);
					sb.append("] ");
					sb.append(variable.toString());
					values.put(variable, sb.toString());
				}
			}
		}
	}

	private String md5sum(String in) {
		try {
			byte[] bytesOfMessage = in.getBytes(Charset.forName("UTF-8"));
			MessageDigest md = MessageDigest.getInstance("MD5");
			return DatatypeConverter.printHexBinary(md.digest(bytesOfMessage));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("MD5 not supported by platform.");
			return null;
		}
	}

	private void appendValue(StringBuilder sb, Job j, Port p) {
		sb.append(p.getIdentifier());
		sb.append('=');
		sb.append(values.get(j.bindings.get(p)).replace('/', '#'));
	}

	public String getValue(Location address) {
		return values.get(address);
	}

	public MutableWorkflow getWorkflow() {
		return workflow;
	}
}
