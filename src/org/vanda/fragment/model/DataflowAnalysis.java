package org.vanda.fragment.model;

import org.vanda.util.TokenSource.Token;
import org.vanda.workflows.elements.Literal;
import org.vanda.workflows.elements.Tool;
import org.vanda.workflows.immutable.AtomicImmutableJob;
import org.vanda.workflows.immutable.ImmutableWorkflow;
import org.vanda.workflows.immutable.JobInfo;

// XXX removed: handle ports (see older versions)
// XXX I've had it with all of this instanceof/typecast bullshit
// find a principled solution (e.g., visitor)
public final class DataflowAnalysis {
	public static final String UNDEFINED = "UNDEFINED";
	
	private final String[] values;
	private final ImmutableWorkflow workflow;

	public DataflowAnalysis(ImmutableWorkflow iwf) {
		workflow = iwf;
		int varcount = 0;
		for (JobInfo ji : iwf.getChildren()) {
			for (Token t : ji.outputs) {
				if (t.intValue() >= varcount)
					varcount = t.intValue() + 1;
			}
		}
		values = new String[varcount];
		for (JobInfo ji : workflow.getChildren()) {
			if (ji.connected) {
				if (ji.job instanceof AtomicImmutableJob) {
					AtomicImmutableJob aj = (AtomicImmutableJob) ji.job;
					if (aj.getElement() instanceof Literal) {
						Literal lit = (Literal) aj.getElement();
						values[ji.outputs.get(0).intValue()] = lit.getValue();
					} else if (aj.getElement() instanceof Tool) {
						StringBuilder sb = new StringBuilder();
						sb.append('(');
						if (ji.inputs.size() > 0) {
							sb.append(values[ji.inputs.get(0).intValue()].replace(
									'/', '#'));
						}
						for (int i = 1; i < ji.inputs.size(); i++) {
							sb.append(',');
							sb.append(values[ji.inputs.get(i).intValue()].replace(
									'/', '#'));
						}
						sb.append(')');
						String s = sb.toString();
						for (int i = 0; i < ji.outputs.size(); i++) {
							values[ji.outputs.get(i).intValue()] = Fragment
									.normalize(aj.getElement().getId())
									+ s
									+ "."
									+ Integer.toString(i);
						}
					}
				}
			} else {
				for (int i = 0; i < ji.outputs.size(); i++) {
					StringBuilder sb = new StringBuilder();
					sb.append('[');
					sb.append(UNDEFINED);
					sb.append("] x");
					sb.append(ji.outputs.get(i).intValue());
					// values[ji.outputs.get(i).intValue()] = UNDEFINED;
					values[ji.outputs.get(i).intValue()] = sb.toString();
				}
			}
		}
	}

	public String getValue(Token address) {
		if (address != null && address.intValue() < values.length)
			return values[address.intValue()];
		else
			return null;
	}
	
	public ImmutableWorkflow getWorkflow() {
		return workflow;
	}
}
