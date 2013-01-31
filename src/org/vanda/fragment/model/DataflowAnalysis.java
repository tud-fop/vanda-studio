package org.vanda.fragment.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public DataflowAnalysis(MutableWorkflow iwf, Job[] sorted) {
		workflow = iwf;
		values = new HashMap<Location, String>();
		jobs = sorted;
	}
	
	public Job[] getSorted() {
		return jobs;
	}
	
	public void init() {
		if (jobs == null)
			return;
		for (Job ji : jobs) {
			if (ji.isConnected()) {
				if (ji.getElement() instanceof Literal) {
					Literal lit = (Literal) ji.getElement();
					values.put(ji.bindings.get(lit.getOutputPorts().get(0)),
							lit.getValue());
				} else if (ji.getElement() instanceof Tool) {
					StringBuilder sb = new StringBuilder();
					sb.append('(');
					List<Port> ports = ji.getInputPorts();
					if (ports.size() > 0)
						appendValue(sb, ji, ports.get(0));
					for (int i = 1; i < ports.size(); i++) {
						sb.append(',');
						appendValue(sb, ji, ports.get(i));
					}
					sb.append(')');
					String s = sb.toString();
					for (Port op : ji.getOutputPorts()) {
						values.put(ji.bindings.get(op),
								Fragments.normalize(ji.getElement().getId()) + s
										+ "." + op.getIdentifier());
					}
				}
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
